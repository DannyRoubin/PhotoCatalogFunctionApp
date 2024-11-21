# Photo Catalog Function App
## What is this?
This app was created to serve as a function app for my photo catalog app.
This app has three main functions
1. This app receives an image and passes the image to Azure computer vision to generate tags
2. Once tags have been generated, the image is stored in Azure blob storage
3. Once the image was successfully saved, the tags are passed to an AWS Lambda function to be stored in a DynamoDB table
