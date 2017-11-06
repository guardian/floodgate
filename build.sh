#!/bin/bash

nvm_installed() {
  if [ -d '/usr/local/opt/nvm' ] || [ -d "$HOME/.nvm" ]; then
    true
  else
    false
  fi
}

nvm_available() {
  type -t nvm > /dev/null
}

if ! nvm_installed; then
  curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.31.2/install.sh | bash

  nvm install
else
  if ! nvm_available; then
    source $HOME/.nvm/nvm.sh
  fi
  nvm install
fi

nvm use
npm install
npm run build

