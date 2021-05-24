const path = require('path');

module.exports = {
  entry: "./public/app.js",
  output: {
    filename: "app.js",
    path: path.resolve("./public/build")
  },
  devtool: 'source-map',
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
      },
      {
        test: /\.s[ac]ss$/i,
        use: [
          "style-loader",
          "css-loader",
          {
            loader: "sass-loader",
            options: {
              sassOptions: {
                includePaths: [path.resolve(__dirname, '../style')],
              },
            },
          },
        ],
      },
    ]
  },
  resolve: {
    modules: ['node_modules'],
    extensions: ['.js', '.jsx', '.json', '.scss']
  }
};
