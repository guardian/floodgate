#!/usr/bin/env bash

printf "\n\rSetting up Floodgate client side dependancies... \n\r\n\r"
printf "\n\rInstalling NPM packages... \n\r\n\r"

npm install

printf "\n\rCompiling Javascript... \n\r\n\r"

npm run build

printf "\n\rDone.\n\r\n\r"
