#!/bin/sh

docker build -t ecommerce_backend .

docker run -it -p 9000:9000 ecommerce_backend