## WORKFLOW

Key points:
* All work should be based on, and merged back, to `master` branch as the main branch
* Work must be reviewed by another developer before merging back up to `master`

### DEVELOPMENT WORKFLOW
1. Create a new git branch

    1.1. Create it off of `master` branch (unless you need to base it off work that only exists in another branch) 
    
    1.2. Give the branch a name
        
        1.2.1. the branch name should be descriptive, brief, and similar to the Trello card title
        
        1.2.2. it should be in kebab-case - lower-case words seperated by hyphens - e.g. run-cli-jar-from-bash
        
        1.2.3. at the end of the branch name, put the Trello card ID, which is the second-last part of the URL i.e. https://trello.com/c/<card-id-here>/card-title-here
        
        1.2.4. be careful to preserve the case of letters in the Trello card ID, they are case-sensitive
   
        1.2.4. so a complete branch name will look like e.g. `run-cli-jar-from-bash-xNgyINDc`

    1.3. Put a link to the branch in the Trello ticket - either simply a URL link in the description, or using the Bitbucket integration (POWER-UPS > Bitbucket > Attach recent branches)

2. Do the work on the branch
    
    2.1. It may help to merge from `master` into your branch on a regular basis to ensure you don't end up with a bunch of merge conflicts in one go at the end
    
    2.2. Where applicable, make sure you write unit tests and functional tests for the work you've done (or write them first as in Test Driven Development). Make sure your tests check your code works when it should, and causes exceptions when it should.
   
    2.3. Add a note for each user-relevant change into the 'NEW CHANGES' section at the top of `CHANGELOG.md`
    
    2.4. Update USER-GUIDE.md as applicable

3. Create a 'pull request' in Bitbucket for your branch back up to `master` when your work is complete

    3.1. Assign the pull request for review to team member(s) responsible the applicable areas (see 'AREAS OF RESPONSIBILITY' below)
    
    3.2. Send reviewer(s) a link via Slack

    3.3. Make sure to add a note in the pull request description (and perhaps slack messages) that lets people know about your requirements for review - e.g. you need all assigned reviewers to approve it, you need everyone to read it, you need particular people to check particular parts of the diff and approve, etc.
    
    3.4. Creators of pull requests should take responsibility for chasing up reviewers, in order to keep development moving quickly. Don't just create a pull request and then wait for days, make sure you get some kind of acknowledgement from the reviewer and chase them up to get the changes merged.
    
    3.5. Don't depend on reviewers to do your work for you. Make sure you check the diff of your code for correctness of logic, style, commenting etc. before you create a pull request and hand off to a reviewer.

FULL GUIDE HERE: https://www.atlassian.com/git/tutorials/comparing-workflows/feature-branch-workflow

### REVIEW WORKFLOW

1. Read Trello card so you know what the code should be achieving
    
    1.1. If the Trello card doesn't contain enough detail, ask the pull request creator to add sufficient detail to the card

2. Run automated tests (unit/functional/integration)

    2.1. If unit tests don't exist, and you think they should, then leave a comment on the pull request asking the creator to write them

3. Read code diff on pull request, leaving comments or questions relating to logic, style, commenting, or any other factor

    3.1. Make sure to read the test code to check it tests all functionality, and will not only pass when it should, but also fail when it should.
    
4. Be as thorough as possible, and make sure you understand what the code is doing. We want more than one person to understand every part of the project. Ask questions with comments on the pull request.

5. Contact the creator of the pull request by Slack when you are done adding your feedback/questions.

6. Approve the pull request once all your feedback and questions have been satisfactorily addressed.

7. If all assigned reviewers have approved, hit the merge button on the pull request page to merge the changes up to `master` branch.

### CODE STYLE LINTER GUIDE

File > Settings > Editor > Code Style > Java > Scheme > gear icon > Import > IntelliJ IDEA Code Style XML

Then select the file 'intellij-java-google-style.xml' from the root directory and hit apply.
Also, double the tabs/indents from 2,2,4 to 4,4,8

Run it using 'Reformat' function (in right-click menu for a whole directory or project, or Code > Reformat Code for an individual file). I used a '*.java' file mask.


### PROTOTYPE RELEASE WORKFLOW

1. Update the version in the pom.xml (it is the `<version>` tag that is the direct child of the `<project>` tag, should be at around line 10, and look something like `<version>1.0-SNAPSHOT</version>`)

2. Use `mvn package` to generate a .jar with bundled dependencies (you will find this in the `target` directory)

3. Update `CHANGELOG.md`

    3.1. Add a new release heading with appropriate prototype number and date.
    
    3.2. Move all items from 'NEW CHANGES' into the new heading. 

    3.3. Make sure the changelog reflects all user-relevant changes that have been merged to master since the last release.

4. Update `USER-GUIDE.md` if needed

    4.1. Diff `USER-GUIDE.md` against the last release, and check the changes against your newly updated `CHANGELOG.md` quickly to see if there's anything obviously missing.

5. Create a new branch named `prototype<version number>` with these changes as a snapshot. This branch should be considered as a historical record only, no changes should be made to it, and no branches should be created from it.

6. Remove the 'NEW CHANGES' section from 'CHANGELOG.md' in this branch because otherwise it'll be confusing for the users.

7. Make a zip file including the following files:

    * `neodynamica.jar` - rename the generated .jar to this name
    * `nda`
    * `USER-GUIDE.md`
    * `CHANGELOG.md`
    
8. Name the zip with the version number and send

### AREAS OF RESPONSIBILITY
As discussed, there should be no area of the project that is understood by only one person. Pull requests / code reviews should be assigned to someone (other than the writer of the branch that is to be reviewed) as per the following:

#### _example area heading_
* Name


---

## USAGE GUIDE FOR DEVELOPERS

### Running CLI

#### Running CLI under bash

The CLI can be run in bash from two sources:
1. FROM JAR: Make sure the .jar is named `neodynamica.jar` and is in the same folder as the shell script file `nda`. Releases should be JAR files and therefore use this method.
2. FROM LOCAL PROJECT CLASS FILES: Make sure there is no JAR named `neodynamica.jar` in the same folder as the shell script file `nda`. This is currently dependent on local machine setup (this may be solved at a later time) and you may need to customise the command in that part of the NDA script and so you may need to customise the command in the `nda` script by copying the command IntelliJ uses when you run the CLI directly in the IDE.

To run the `nda` shell script (for both of the above options), either:
* Add the directory containing the  `PATH` and run:
    1. `PATH="/path/to/this/directory/:$PATH"` 
    2. (optional) Add the above line to your `.bash_profile` as well so it'll run every time you log in
    3. Test with `nda --help`
* Run using `source`: 
    * `source ./nda --help` 
    * Or: `. ./nda --help` (`.` is equivalent to `source`)

In both cases you may need to set the file as executable with something like: `chmod 774 nda`

#### Running CLI from IDE

To run the CLI directly from the IDE without having to consider the system shell and environment, simply run the CLIPrompt class and enter your commands `nda [...]` there. This does not have any advanced terminal functionality like history and autocompletion, so running it from bash is usually advised.

#### Options and configuration

You must always specify a configuration file with the option `-c example.config`

The order of precedence of all other options is:
1. Command-line options (see `nda --help`)
2. Specified configuration (`-c` option)
3. Default configuration file (hard-coded)
