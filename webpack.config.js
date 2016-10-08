const _ = require('lodash');
const path = require('path');
const minimist = require('minimist');
const autoprefixer = require('autoprefixer');
const webpack = require('webpack');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CleanWebpackPlugin = require('clean-webpack-plugin');

const DEFAULT_TARGET = 'DIST';

const DEFAULT_PARAMS = {
    entry: [
        './ui-implicit/src/main/frontend/app'
    ],
    output: {
        filename: 'js/[name]-[hash:6].js',
        sourceMapFilename: 'js/[name]-[hash:6].map'
    },
    plugins: [
        new HtmlWebpackPlugin({
            template: './ui-implicit/src/main/frontend/index.html',
            chunksSortMode: 'dependency',
            minify: false,
        }),
    ],
    module: {
        loaders: [
            {
                test: /\.html$/,
                exclude: /(index\.html$|home\.html$)/,
                loader: 'file?name=partials/[name]-[hash:6].[ext]'
            }
            , {
                test: /\.(ico|png|jpg|gif|svg|eot|ttf|woff|woff2)(\?.+)?$/,
                loader: 'url?limit=50000,name=images/[name]-[hash:6].[ext]'
            }
            , {
                test: /\.js$/,
                exclude: /(node_modules)/,
                loader: 'ng-annotate!babel?presets[]=es2015',
                include: path.join(__dirname, 'ui-implicit/src/main/frontend')
            }
            , {
                test: /\.css$/,
                loader: 'style!css'
            }
            , {
                test: /\.scss$/,
                loader: 'style!css!autoprefixer!sass'
            }
            , {
                test: /\.json/,
                loaders: ['json-loader']
            }
        ]
    },
    postcss: [
        autoprefixer({
            browsers: ['last 2 versions']
        })
    ],
};

const PARAMS_PER_TARGET = {

    DEV: {
        devtool: 'inline-source-map',
        metadata: {
            ENV: 'dev',
            host: 'localhost',
            port: 3000
         },
        output: {
            path: './ui-implicit/src/main/resources/static/',
            publicPath: 'http://localhost:3000/',
            filename: '[name].bundle.js'
        },
        plugins: [
        ],
        devServer: {
            port: 3000,
            contentBase: './ui-implicit/src/main/frontend',
            proxy: {
                '/user': 'http://localhost:8080/'
            },
        },
    },
    DIST: {
        debug: false,
        output: {
            path: path.join(__dirname, 'dist'),
        },
        plugins: [
            new CleanWebpackPlugin(['dist']),
            new webpack.optimize.UglifyJsPlugin({
                compressor: {
                    warnings: false
                }
            }),
        ]
    }
};

function _resolveBuildTarget(defaultTarget) {
    let target = minimist(process.argv.slice(2)).TARGET;
    if (!target) {
        console.log('No build target provided, using default target instead\n\n');
        target = defaultTarget;
    }
    return target;
}

function _mergeArraysCustomizer(a, b) {
    if (_.isArray(a)) {
        return a.concat(b);
    }
}

const target = _resolveBuildTarget(DEFAULT_TARGET);
const params = _.mergeWith(DEFAULT_PARAMS, PARAMS_PER_TARGET[target], _mergeArraysCustomizer);

module.exports = params;
