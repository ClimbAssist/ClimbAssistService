Contributing
------------

Thanks for considering contributing to Climb Assist! This is a big endeavor and we need help from folks like you to make
it great. We're new to being open source, so please let us know if you have any suggestions on how we can make things
better.

I Have a Bug/Feature Request
----------------------------

Great! Submit it [here](TODO). If it's a bug, be sure to include the date and time when you experienced, as
well as reproduction steps. If it's a feature request, try to be crisp and break it into manageable tasks. We only have
one maintainer right now, so we can't guarantee that we will be able to look at it right away, but we'll do our best! 

I Want to Help
--------------

Lovely! If you're looking to help with back-end development, you're in the right place. If you're more
interested in front-end development, check out [ClimbAssistUI](TODO). 
 
Check out the [README](README.md) for details on cloning, running, and testing. In general, features should be
implemented in a new branch, and if they correspond with a bug/feature request, it's useful to include the tracking
number in the branch name. Additionally, please ensure than any new or modified APIs are reflected in 
[docs/api_documentation.md](docs/api_documentation.md). Once you've made code changes and you're confident that they
work, submit a pull request and one of our moderators will take a look at it as soon as they can. We only have one
maintainer right now, so it might take some time before your request can be looked at, but we promise we will review
it as soon as we can!

I Want to Help but I Don't Know Anything About Climbing!
--------------------------------------------------------
That's fine! You don't need a super in-depth knowledge of climbing to help. At least 50% of ClimbAssistService has
nothing to do with climbing. Dive right in!

That being said, hit us up some time if you want an intro to climbing :)

I Want to Help but I Don't Know How to Code!
--------------------------------------------
That's fine too! We have lots of ways for non-developers to help. Checkout the 
[contribution page](https://climbassist.com/contribute) on the website for more information.

Roadmap
-------

Obviously you're welcome to contribute whatever features you want to, but if you're looking for some ideas, this is a
rough breakdown of the goals we have for the foreseeable future, roughly in priority order.

1. Launch a mobile application. This is a biggie and it might warrant its own repository and infrastructure set-up, so
it might be best to reach out to the development team at [dev@climbassist.com](mailto:dev@climbassist.com) if you're
really passionate about this.
1. Create profiles for returning users. Climb Assist already has a user system, but it's currently hidden on the website
because there aren't any useful features associated with being a user. It would be great to allow users add some
information like their location, climbing grade, and photos from their past climbs. It would also be useful for users to
be able to save routes that they have previously climbed or want to climb.
1. Allow users to rank and comment on routes. Comments are fairly straightforward, but ranks could be more
complicated. We can start with just an individual ranking, where users could return to a route and see how they had
previously ranked it. Later we can implement a crowd-ranking system where all user rankings will be aggregated and
shown along with the route information for everyone.
1. Migrate to a graph database. The current system is built using several levels of DynamoDB tables, which works okay
but don't scale as well as we would like. Migrating all of our data and APIs to a graph database (probably 
[Neptune](https://aws.amazon.com/neptune/)) and integrating with GraphQL would speed up requests and make everything a
lot simpler.
1. Allow users to contribute new crags, walls, and routes. The website currently has an editor, but only
administrators can use it. We need to implement a way for non-administrator users to propose additions or modifications
to our data where they can be reviewed by moderators and then approved to be shown to all users.
