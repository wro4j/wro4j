# About
This page describe the steps to follow when releasing a new version of wro4j.

  * Run ```mvn release:prepare```
  * Run ```mvn clean deploy -Psonatype-oss-release```. The deploy should be signed by pgp in order to be promoted. This one will create a staging release on [nexus repository](http://oss.sonatype.org/)
  * Merge the working branch into the master branch
  * Close & promote the staging. During the next 1 hour, the release will be available on maven central repo.
  * Create a tag for newly released version ```git tag v1.2.6```
  * Push all the changes to remote git repository ```git push --tags```
  * Update the [home page news section](http://code.google.com/p/wro4j/) & update reference to the latest version in all wiki pages
  * Send email on [mailing list](http://groups.google.com/group/wro4j)
  * Synchronize svn with master branch from git.
  * Upload new artifact to Downloads page & make latest version a featured download

# Useful links 
  
  * [OSS Repository Hosting](http://nexus.sonatype.org/oss-repository-hosting.html)