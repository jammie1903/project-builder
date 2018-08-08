# Contributing

If you wish to make a contribution to this project, please make sure there is a relevant issue created for it first. Also, please assign (or ask a contributor to assign) the relevant issue to yourself whislt working on it, to avoid any wasted effort by another developer.

## Pull Request Process

- One branch per issue, please label both the branch and the commit with the id of the issue, and preferably add a relevant desciption e.g. 6-contributors_guide for the branch name and "#6: Adding contributors guide" for the commit message. These are just examples, I'm not too fussy as long as there is a clear link;
- You may merge the Pull Request in once you have the sign-off of any contributor, or if you do not have permission to do that, you may request the reviewer to merge it for you.

## Releases

- The release process is unfortunantly heavily tied to intellij, as it relies on the "artifacts" created by the IDE's build process. If you do not use intellij, or do not feel confident performing a release, you can request a release from me (jammie1903).

- A release will be necessary for any users of the software to see your changes, merging to master is not enough!

### Before the release
- Ensure the version file is updated relevantly and this change is committed `src/resources/version.txt`
- Make sure the artifact has been successfully built. You can run it for testing purposes straight out of the artifacts folder.

### Preforming a release
project-builder has its own built in release script, found in `/src/main/com/jamie/releaser/Release.java`, simply run this, and it will release the built artifact to the repo.
