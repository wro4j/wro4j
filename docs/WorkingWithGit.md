# Introduction
Main project page is hosted by google code, but the main development occurs on github, mostly because github was the first code collaboration application using git SCM. This page describes how git is used by wro4j and the details about branching naming conventions and way of working.

# Details 
Conventions:
  * The project has more than one branch. 
  * The main development doesn't occur on master branch. Master is used only to hold the versions of the last release. 
  * Tags are prefixed with *v* followed by the version number. Ex: *v1.0*
  * Main development branches contains version number followed by *.x*. Ex: *1.4.x*. The *.x* is the part from where minor versions will be created. For instance, if latest stable release is *1.4.3*, the development branch is *1.4.x*, next release will be *1.4.4*.
  * There can be multiple development branches. Ex: *1.4.x*, *1.5.x*. This does make sense when there are major changes or feature additions.       
  * Feature branch - is a branch which contains only the changes associated with a single issue. This is useful, in order to easily merge feature branches into different development branches.
  * The name of the feature branch can be anything which suggest somehow the details about the feature. Example: wildcard, modelUpdate, etc. Or even better, you can name the branches with the issue number from the issue tracker. Example: **issue331**, **issue344**, etc (this helps to easily understand what kind of work is going on a particular branch).
  * Each feature branch is merged into main development branch after it is completed and unit tested.

Examples:

### Starting to work on a new feature

#### Checkout the main development branch
```
git checkout 1.4.x
``
#### Create the feature branch from main development branch. Here we create a new branch called {{{issue321}}} starting from the main development branch 1.4.x:
```
git checkout -b issue321
```
####  Work on that branch and commit as often as you need your changes. These can be unstable also.
```
git commit -m "start implementation"
```
#### When the feature is ready, it can be merged back into main development branch. 
```
git checkout 1.4.x
git merge issue321 --no-ff
```
You can notice the following: 
  * first we switch back to main development branch
  * next we merge all feature commits into 1.4.x
  * **--no-ff** option is useful to preserve the history in a nice way.
 
#### Push the changes to origin
```
  git push origin 1.4.x
```

The steps described above, can be visualized as this: 
```
(issue321)*----*------*-------*
        |                      |
        |                      |
(1.4.x)*-----------------------*-----------
```

You can also take a look on [network graph](https://github.com/wro4j/wro4j/network).

## Useful Git Commands 
| *Command* | *Description* |
| git clone *url* | Clone a remote git repository |
| gitk --all | See entire history as tree |
| git fetch | Get latest changes from remote repository, without local override |
| git add *path* | Files to add to staging area (files to commit). Supports wildcards |
| git status | Shows the files which are in stage area and untracked files |
| git commit -am "comment message" | Commits all the files from the staging area to local repository |
| git checkout -b newbranch | Creates a branch called 'newbranch' and performs a checkout in the same time |
| git checkout mybranch | Checkout the branch called 'mybranch' |
| git merge mybranch --no-ff | Merges the current branch with 'mybranch'. That means that changes from 'mybranch' are broght to current branch. |
| git push origin mybranch | Push the branch 'mybranch' to remote repository |
| git clean -f  | Removes all unstaged files |
| git gui | Git gui utitlity. Helps you to visualize staged and anstaged files |
| git remote add git@github.com:wro4j/wro4j.git  | Adds a remote repository. This helps to track & merge changes from other repositories other than origin. |