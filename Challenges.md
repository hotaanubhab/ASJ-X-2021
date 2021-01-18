## Learning the new gradle build version and modifiying build.gradle.
 * We updated from an older gradle version and had to face issues with using outdated code we used.
 * We had understand and playaround with the buildscript's SDK to start executing a basic layout so that further work on project can continue.

## There were problems in learning androidx libraries and reusing them in place of android support libraries.
 * Some of the functions that we used from out past knowledge required Android Support Libraries that no longer work with new SDK.
 * We had to approriate convert to the new AndroidX libraries wherever needed. 
## Issues with manifest files and crashing the app, troubleshooting connection to opentrivia using ok http.
 * Multiple activities were causing error,when switching between and we had to add intent filters.
 * Needed to add INTERNET permission in manifest for OKHttp3 to work. 
## Loading dynamic layout from fetched data.
 * Use of a OpenTrivia.kt to handle JSON response from API.
## UI stylying and image and widget placing and also learning to use constraint layout.
 * Understanding the Contraint Layout for our needs and modifying wherever needed,had prior experience with only Linear and Relative Layout 