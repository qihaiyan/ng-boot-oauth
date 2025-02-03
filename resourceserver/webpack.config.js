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
        './src/main/frontend/app'
    ],
    output: {
        filename: 'js/[name]-[hash:6].js',
        sourceMapFilename: 'js/[name]-[hash:6].map'
    },
    plugins: [
        new webpack.LoaderOptionsPlugin({
            options: {
                postcss: [
                    autoprefixer({
                        browsers: ['last 2 versions']
                    })
                ]
            }
        }),
        new HtmlWebpackPlugin({
            template: './src/main/frontend/index.html',
            chunksSortMode: 'dependency',
            minify: false,
        }),
    ],
    module: {
        rules: [
            {
                test: /\.html$/,
                exclude: /(index\.html$$)/,
                use: [
                    {
                        loader: 'file-loader',
                        options: {
                            name: 'partials/[name]-[hash:6].[ext]'
                        }
                    }
                ]
            }
            , {
                test: /\.(ico|png|jpg|gif|svg|eot|ttf|woff|woff2)(\?.+)?$/,
                use: [
                    {
                        loader: 'url-loader',
                        options: {
                            limit: 50000,
                            name: 'images/[name]-[hash:6].[ext]'
                        }
                    }
                ]
            }
            , {
                test: /\.js$/,
                exclude: [
                    path.resolve(__dirname, 'node_modules')
                ],
                use: [
                    'ng-annotate-loader',
                    'babel-loader'
                ],
                include: path.join(__dirname, 'src/main/frontend')
            }
            , {
                test: /\.css$/,
                use: [
                    'style-loader',
                    'css-loader'
                ]
            }
            , {
                test: /\.scss$/,
                use: [
                    'style-loader',
                    'css-loader',
                    'autoprefixer-loader',
                    'sass-loader'
                ]
            }
        ]
    },
};

const PARAMS_PER_TARGET = {

    DEV: {
        output: {
            path: path.resolve(__dirname, 'src/main/resources/static'),
            publicPath: 'http://localhost:3000/',
            filename: 'bundle.js'
        },
        plugins: [
        ],
        devServer: {
            port: 3000,
            contentBase: './src/main/frontend',
            headers: {
                'Access-Control-Allow-Origin': 'http://localhost:8080',
                'Access-Control-Allow-Credentials': 'true',
                'Access-Control-Allow-Headers': 'authorization',
                'Access-Control-Allow-Methods': 'GET'
            },
            proxy: {
                '/user': 'http://localhost:8080/'
            },
        },
    },
    DIST: {
        output: {
            path: path.join(__dirname, 'dist'),
        },
        plugins: [
            new CleanWebpackPlugin(['dist']),
            new CleanWebpackPlugin(['src/main/resources/static']),
            new webpack.optimize.UglifyJsPlugin({
                sourceMap: true
            }),
        ]
    }
};

function _resolveBuildTarget(defaultTarget) {
    let target = minimist(process.argv.slice(2)).env.TARGET;
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
