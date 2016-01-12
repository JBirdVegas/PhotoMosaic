# PhotoMosaic

Creates a photo mosaic for a given target image from a collection of images.

## Maven builds

##### Build, with maven:
(requires Java 8 or higher)
```
mvn clean install
```

##### Usage, if built from maven:
```
java -jar target/photo-mosaic.jar -d <input directory> <target filename>
```

## Gradle builds
```
# build jar
gradle clean build

# use jar
# version postfix depends on version defined by `build.gradle`
java -jar build/libs/photo-mosaic-SNAPSHOT-0.1.jar <input directory> <target filename>
```

##### Current TODOs:
- [x] make it basically work
- [x] use args4j to collect command line arguments
- [ ] tidy up / refactor
- [ ] support reading the images of the collection recursively from given directory
- [ ] introduce different strategies for placing the tiles
- [X] use multiple threads

## My first test result
Used a tile size of 40x30 pixels. The provided image collection was quite poor. Nevertheless, I like it :smile:

<img src="https://github.com/jenshadlich/PhotoMosaic/blob/master/data/first_mosaic_result_400x300.png" alt="alt text" width="400" height="300">

##### Original image
<img src="https://github.com/jenshadlich/PhotoMosaic/blob/master/data/first_mosaic_input_400x300.png" alt="alt text" width="400" height="300">

