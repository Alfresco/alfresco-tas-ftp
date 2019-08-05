:paw_prints:  Back to Utility [README](README.md).

---
# Change Log
All notable changes to this project will be documented in this file.
(if you need to update/polish tests please branch from the release tags)

Each tag bellow has a corresponded version released in [Nexus](https://nexus.alfresco.com/nexus/#welcome).

## Alfresco Matrix vs TAS Releases

## [[v5.2.0-2] - 2017-02-14](/tas/alfresco-tas-ftp-test/commits/v5.2.0-2)
### Added
- update utility to 1.0.10
- fix rename tests
- fixed tests due to new lastContentModel changes
- remove tenant tests
- updated usingResource and rename methods to handle the lastContentModel
  locations
- added modification time
- fix rename (cherry picked from commit 266757a)
- add tests to delete locked file
- cp use FtpReply
- update tests to use FtpReply instead of int
- fix userIsLoggedIn() method
- update ftp-suites.xml
- update bambooRun.sh - master
- update ftp-shutes.xml
- update bambooRun.sh - master
- fix bambooRun.sh
- renamed test case
- update 5.1.N version
- test- added new test cases- FULL
- fix siteManagerIsNotAbleToCreateFileTwiceInSameLocation test
- add test to verify user authorization status.
- rename DeleteFolderTests
- test- updates after review
- rename class RenameFolderTests + disconect users from tests
- test- added new test cases - Full
- add full tests for rename file
- 'TAS-2841: add full tests for connection'
- added tests - tenant
- test- added test cases - Full
- add full tests for append and update content
- fix Jenkinsfile
- close input streams
- added suite
- add full tests for update content
- test- added new test cases- full suite
- 'add tests: upload inexistent file, get file for tenant from other network
  and copy locked file'
- fix bambooRun.sh
- 'TAS-2851: modify upload FTP method. Add full tests. (Contains also TAS-2853)'
- 'TAS-2864: add test to delete locked file'
- updated scenarios that use special characters in name
- try to fix siteManagerIsNotAbleToCreateFolderWithSpecialCharsInName
- fix siteContributorShouldCopyFileAddedByOtherUser() test
- removed useless lines
- fix tests from create file / folder and change working directory
- add full group in ftp-suites.xml. Fix test from create file and folder
- set 1 thread on ftp-runner-suite/.xml
- uncomment tests with size
- fix copyTo FTP action + tests
- add check to verify if source exists copyTo()
- test- added new testcases
- add 1 thread to ft-runner-suite. Fix tests from create file and folder
- fix copyTo() method
- add full tests for set and get modification date using FTP
- removed unused imports
- test- updated tests after review
- updated copyTo method
- TAS-2858, TAS-2859, TAS-2860, TAS-2861, TAS-2862
- fix Jenkinsfile
- fix Jenkinsfile
- updates
- 'updated - test case: tenantUserIsNotAbleToCopyFile'
- TAS-2843 change directory
- test - added testcases for Full - CopyFile
- TAS-2848 create file
- TAS-2844 create directory
- added testCount.xml
- fix Jenkinsfile
- fix default.properties
- Add proper word document
- Merge branch 'createOfficeFile' into 'master'
- Create Office file that will be used by AOS in integration scenarios (content
  not corrupted)
- 'Changed JenkinsFile: specify if @bug tests are running or not'
- 'Changed JenkinsFile: specify if @bug tests are running or not'
- archive logs
- enable TestRail listener
- updated log4j
- added 5.2 default propertie
- added unit group to tests
- Updated README.md
- ftp jenkinsfile default test server
- 'TAS-2149: fix copy FTP action(add test to copy folder with multiple children).
  TAS-2147: add core test for change directory action'
- 'TAS-2149: fix copy FTP action(add test to copy folder with multiple children).
  TAS-2147: add core test for change directory action'
- 'add: jenkinsfile passiveMode'
- updated to exclude networks
- 'jenkins: enable jolokia by default'
- updated to exclude networks
- 'jenkinsfile: exclude networks (tests with tenant users) by default'
- Merge branch 'TAS-2159' into 'master'
- Added MoveToAFile Core Tests
- Added FunctionalCasesTests
- TAS-2146 created GetDir with tenant users tests
- 'TAS-2323: add sanity and core tests for storeFileStream FTP action. Contains
  also TAS-2153'
- TAS-2146 created GetDir with tenant users tests
- 'TAS-2323: add sanity and core tests for storeFileStream FTP action. Contains
  also TAS-2153'
- Copy empty/non empty directory
- added tests
- Copy empty/non empty directory
- added tests
- 'TAS-2160: add tests for set transfer mode'
- 'TAS-2160: add tests for set transfer mode'
- update utility version to 1.0.7-SNAPSHOT
- fix anyUserIsAbleToRetrieveRootDirectory() test
- fix anyUserIsAbleToRetrieveRootDirectory() test
- 'TAS-2145: add sanity and core tests for connection to FTP. Add method to
  verify the reply code returned by FTP client.'
- 'TAS-2151/TAS-TAS-2148: add core tests for create file. Add core tests for
  create folder'
- update utility version to 1.0.7-SNAPSHOT
- 'TAS-2145: add sanity and core tests for connection to FTP. Add method to
  verify the reply code returned by FTP client.'
- 'TAS-2151/TAS-TAS-2148: add core tests for create file. Add core tests for
  create folder'
- fix jenkins file
- fix jenkins file
- 'add: jenkinsfile'

## [[v5.2.0-1] - 2016-12-23](/tas/alfresco-tas-ftp-test/commits/v5.2.0-1)
### Added

- added tests that assures 100% test coverage for Alfresco 5.2.N

## [[v5.2.0-0] - 2016-12-12](/tas/alfresco-tas-ftp-test/commits/v5.2.0-0)
### Added

- tests for Alfresco 5.2

