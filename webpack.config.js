const path = require("path");

const webpack = require("webpack");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const nodeExternals = require("webpack-node-externals");
const {BundleAnalyzerPlugin} = require("webpack-bundle-analyzer");

const is_production = process.env.NODE_ENV === 'production';

const config = {
    target: "node",
    entry: {
        index: "./src/index.ts"
    },
    output: {
        filename: "[name].js",
        path: path.join(__dirname, "dist/")
    },
    module: {
        rules: [{
            test: /\.ts$/,
            use: {
                loader: "ts-loader"
            },
            exclude: /node_modules/,
        }]
    },
    resolve: {
        extensions: [".ts", ".js", ".json"],
    },
    externals: [
        nodeExternals()
    ],
    plugins: [
        new webpack.DefinePlugin({
            IS_PRODUCTION: JSON.stringify(is_production),
            IS_DEVELOPMENT: JSON.stringify(!is_production),
        }),
    ]
};


module.exports = () => [config];
