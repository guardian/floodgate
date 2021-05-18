var webpack = require('webpack');
var path = require('path');

module.exports = {
  module: {
    rules: [
      {
        test:    /\.js$/,
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader',
          options: {
            presets: [
                ['@babel/preset-env', { targets: "defaults" }],
                ['@babel/preset-react']
            ],
            plugins: ["transform-object-assign"]
          }
        }
      }
    ]
  },
  plugins: [
    new webpack.DefinePlugin({
      'process.env': {
        'NODE_ENV': '"production"'
      }
    }),
    new webpack.IgnorePlugin(/^\.\/locale$/, /moment$/),
  ],
  resolve: {
    extensions: ['.js', '.jsx', '.json']
  },
  resolveLoader: {
    roots: [path.join(__dirname, '..', 'node_modules')]
  },
};
