const path = require('path');
// eslint-disable-next-line import/order
const merge = require('webpack-merge');
const commonConfig = require('./webpack.config.common');

module.exports = merge.merge(commonConfig, {
  mode: 'development',
  devtool: 'inline-source-map',
  devServer: {
    historyApiFallback: { index: '/', disableDotRule: true },
    static: {
      directory: path.join(__dirname, 'build'),
    },
    proxy: {
      '/extApi': 'http://localhost:8080',
      '/ui': 'http://localhost:8080',
    },
    compress: false,
    port: 3000,
  },
});
