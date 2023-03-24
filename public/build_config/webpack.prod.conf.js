const webpack = require('webpack');
const path = require('path');

module.exports = {
  entry: "./public/app.js",
  output: {
    filename: "app.js",
    path: path.resolve("./public/build")
  },
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
    new webpack.IgnorePlugin( {resourceRegExp: /^\.\/locale$/,contextRegExp: /moment$/}),
  ],
  resolve: {
    extensions: ['.js', '.jsx', '.json']
  },
  resolveLoader: {
    roots: [path.join(__dirname, '..', 'node_modules')]
  },
};
