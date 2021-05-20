var path = require('path');

module.exports = {
  devtool: 'source-map',
  module: {
    loaders: [
      {
        test:    /\.js$/,
        exclude: /node_modules/,
        loaders: ['babel?presets[]=es2015&presets[]=react&plugins[]=transform-object-assign']
      }
    ]
  },
  resolveLoader: {
    root: path.join(__dirname, '..', 'node_modules')
  },

  sassLoader: {
    includePaths: [path.resolve(__dirname, '../style')]
  },

  resolve: {
    extensions: ['', '.js', '.jsx', '.json', '.scss']
  }
};
