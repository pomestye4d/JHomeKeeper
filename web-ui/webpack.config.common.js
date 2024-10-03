// const CopyWebpackPlugin = require('copy-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');

const path = require('path');

module.exports = {
  entry: './index.tsx',
  target: 'web',
  output: {
    path: path.resolve(__dirname, 'build'),
    filename: '[name]-module.js',
  },
  resolve: {
    extensions: ['.ts', '.js', '.tsx'], // resolve all the modules other than index.tsx
    fallback: { timers: require.resolve('timers-browserify') },
  },
  module: {
    rules: [
      {
        loader: 'ts-loader',
        test: /\.(ts|tsx)?$/,
        options: { allowTsInNodeModules: true },
      },
      {
        test: /\.css$/,
        use: [
          'style-loader',
          'css-loader',
        ],
      },
    ],
  },
  plugins: [
    // new CopyWebpackPlugin({
    //   patterns: [
    //     { from: './public' },
    //   ],
    // }),
    new HtmlWebpackPlugin({
      template: 'public/index.html',
    }),
  ],
};
