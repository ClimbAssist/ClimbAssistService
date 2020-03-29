# Important!
# For some reason this import has to be very first, otherwise the cfnresponse module doesn't get included in the Lambda
import cfnresponse
import datetime
import hashlib
import hmac
import json
import logging
import os
import sys

from botocore.vendored import requests

logger = logging.getLogger()
logger.setLevel(logging.INFO)


def sign(key, msg):
    return hmac.new(key, msg.encode('utf-8'), hashlib.sha256).digest()


def get_signature_key(key, date_stamp, region_name, service_name):
    k_date = sign(('AWS4' + key).encode('utf-8'), date_stamp)
    k_region = sign(k_date, region_name)
    k_service = sign(k_region, service_name)
    k_signing = sign(k_service, 'aws4_request')
    return k_signing


# We have to use raw HTTPS requests to the Cognito endpoint because the SDK that runs in Lambda doesn't support the
# VerificationMessageTemplate.DefaultEmailOption yet
def handler(event, context):
    try:
        logger.info('got event {}'.format(event))

        if event['RequestType'] == 'Update':
            cfnresponse.send(event, context, cfnresponse.FAILED, {})
            return

        elif event['RequestType'] == 'Create':
            user_pool_id = event['ResourceProperties']['UserPoolId']
            ses_email_identity = event['ResourceProperties']['SesEmailIdentity']
            logger.info('Setting properties for user pool {}'.format(user_pool_id))

            properties = {
                'UserPoolId': user_pool_id,
                'Policies': {
                    'PasswordPolicy': {
                        'MinimumLength': 8
                    }
                },
                'AutoVerifiedAttributes': ['email'],
                'VerificationMessageTemplate': {
                    'EmailSubjectByLink': 'Climb Assist verification link',
                    'EmailMessageByLink': 'Please click the link below to verify your email address.\n{##Verify '
                                          'Email##}',
                    'DefaultEmailOption': 'CONFIRM_WITH_LINK'
                },
                'EmailConfiguration': {
                    'SourceArn': ses_email_identity,
                    'ReplyToEmailAddress': 'no-reply@climbassist.com',
                    'EmailSendingAccount': 'DEVELOPER'
                }
            }

            request_parameters = json.dumps(properties)

            method = 'POST'
            service = 'cognito-idp'
            region = os.environ.get('AWS_DEFAULT_REGION')
            host = 'cognito-idp.{}.amazonaws.com'.format(region)
            endpoint = 'https://cognito-idp.{}.amazonaws.com'.format(region)
            content_type = 'application/x-amz-json-1.0'
            amz_target = 'AWSCognitoIdentityProviderService.UpdateUserPool'

            access_key = os.environ.get('AWS_ACCESS_KEY_ID')
            secret_key = os.environ.get('AWS_SECRET_ACCESS_KEY')
            session_token = os.environ.get('AWS_SESSION_TOKEN')
            if access_key is None or secret_key is None or session_token is None:
                logger.error('No credentials available.')
                sys.exit()

            t = datetime.datetime.utcnow()
            amz_date = t.strftime('%Y%m%dT%H%M%SZ')
            date_stamp = t.strftime('%Y%m%d')  # Date w/o time, used in credential scope

            canonical_uri = '/'

            canonical_querystring = ''

            canonical_headers = 'content-type:' + content_type + '\n' + 'host:' + host + '\n' + 'x-amz-date:' \
                                + amz_date + '\n' + 'x-amz-target:' + amz_target + '\n'

            signed_headers = 'content-type;host;x-amz-date;x-amz-target'

            payload_hash = hashlib.sha256(request_parameters.encode('utf-8')).hexdigest()

            canonical_request = method + '\n' + canonical_uri + '\n' + canonical_querystring + '\n' \
                                + canonical_headers + '\n' + signed_headers + '\n' + payload_hash

            algorithm = 'AWS4-HMAC-SHA256'
            credential_scope = date_stamp + '/' + region + '/' + service + '/' + 'aws4_request'
            string_to_sign = algorithm + '\n' + amz_date + '\n' + credential_scope + '\n' + hashlib.sha256(
                canonical_request.encode('utf-8')).hexdigest()

            signing_key = get_signature_key(secret_key, date_stamp, region, service)

            signature = hmac.new(signing_key, string_to_sign.encode('utf-8'), hashlib.sha256).hexdigest()

            authorization_header = algorithm + ' ' + 'Credential=' + access_key + '/' + credential_scope + ', ' \
                                   + 'SignedHeaders=' + signed_headers + ', ' + 'Signature=' + signature

            headers = {'Content-Type': content_type,
                       'X-Amz-Date': amz_date,
                       'X-Amz-Target': amz_target,
                       'Authorization': authorization_header,
                       "X-Amz-Security-Token": session_token}

            response = requests.post(endpoint, data=request_parameters, headers=headers)

            if response.status_code != 200:
                logger.error(response.text)
                cfnresponse.send(event, context, cfnresponse.FAILED, {})
                return

        cfnresponse.send(event, context, cfnresponse.SUCCESS, {})
    except Exception as e:
        logger.error('caught exception {}'.format(e))
        cfnresponse.send(event, context, cfnresponse.FAILED, {})
