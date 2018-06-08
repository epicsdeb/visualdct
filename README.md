# VisualDCT (aka VDCT) [![Build Status](https://travis-ci.org/epics-extensions/VisualDCT.svg?branch=master)](https://travis-ci.org/epics-extensions/VisualDCT)

VisualDCT is the Visual Database Configuration Tool for
[EPICS](https://epics.anl.gov/) Databases.

It was developed by [Cosylab](http://www.cosylab.com/).
This development was funded by SLS, APS, Diamond and SNS. 

## Web Site

VisualDCT has a generated web site that contains a wealth of information,
including user manuals, release notes, reference and project documentation.

There are two versions available on-line:

 * [latest release](https://epics-extensions.github.io/VisualDCT/)
 * [development version](https://openepics.ci.cloudbees.com/job/ext-VisualDCT-master-site/site/)

## Downloads

Distribution archives of released versions (containing binaries,
sources, javadoc, and the documentation web site) are available from the
[APS VDCT page](https://epics.anl.gov/extensions/vdct/index.php).

## Building

VisualDCT is a [Maven](https://maven.apache.org/) project.
Any recent Java IDE should be able to open and compile it.

Apart from the default lifecycle, two additional Maven goals are useful:

  * **site** Generate the website with user, reference and project documentation
  * **assembly:single** Generate the distribution archives as tar and zip

## Continuous Integration

CI is provided through Jenkins as part of the
[EPICS Extensions CI](https://openepics.ci.cloudbees.com/view/EPICS%20Extensions/)
on the [Jenkins in the Cloud](https://www.cloudbees.com/products/jenkins-cloud)
platform powered by [CloudBees](https://www.cloudbees.com/),
and on [Travis](https://travis-ci.org/epics-extensions/VisualDCT).

## Bugs

Any bugs should be reported as
[issues](https://github.com/epics-extensions/VisualDCT/issues)
in the GitHub project.

## Contributing

Pull requests are always welcome.

For non-trivial changes and additions, please first contact the authors
and/or use the EPICS
[Tech-Talk](https://epics.anl.gov/tech-talk/index.php) mail exploder
to discuss and review your plans.
