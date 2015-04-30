# PhotoMosaic

Creates a photo mosaic for a given target image from a collection of images.

### Build:
(requires Java 7 or higher)
```
mvn clean install
```

### Usage:
```
java -jar target/photo-mosaic.jar -d <input directory> <target filename>
```

### Current TODOs:
- [x] make it basically work
- [x] use args4j to collect command line arguments
- [ ] tidy up / refactor
- [ ] support reading images for collection recursivly from given directory
- [ ] introduce different strategies for placing the tiles
- [ ] use multiple threads

## My first result
Used a tile size of 40x30 pixels. The provided image collection was quite poor but I like it anyway :)
<img src="https://github.com/jenshadlich/PhotoMosaic/blob/master/data/first_mosaic_result_400x300.png" alt="alt text" width="400" height="300">

## Input image
<img src="https://github.com/jenshadlich/PhotoMosaic/blob/master/data/first_mosaic_input_400x300.png" alt="alt text" width="400" height="300">

