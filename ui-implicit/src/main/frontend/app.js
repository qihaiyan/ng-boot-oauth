import angular from 'angular';
import ngMessages from 'angular-messages';
import ngResource from 'angular-resource';
import ngAnimate from 'angular-animate';
import toastr from 'angular-toastr';
import uirouter from 'angular-ui-router';
import ngSanitize from 'angular-sanitize';
import accountServiceModule from './services/account';
import ctlModule from './controllers/controller.module';
import 'satellizer';
import './css/styles.css';
import './css/angular-toastr.css';

const appModule = angular.module('MyApp', [
  ngMessages,
  ngResource,
  ngAnimate,
  toastr,
  uirouter,
  ngSanitize,
  'satellizer',
  accountServiceModule,
  ctlModule,
]);

export default appModule;

appModule.config(($stateProvider, $urlRouterProvider, $authProvider, $windowProvider) => {
    /**
     * Helper auth functions
     */
    const skipIfLoggedIn = ['$q', '$auth', ($q, $auth) => {
      const deferred = $q.defer();
      if ($auth.isAuthenticated()) {
        deferred.reject();
      } else {
        deferred.resolve();
      }
      return deferred.promise;
    }];

    const loginRequired = ['$q', '$location', '$auth', ($q, $location, $auth) => {
      const deferred = $q.defer();
      if ($auth.isAuthenticated()) {
        deferred.resolve();
      } else {
        $location.path('/login');
      }
      return deferred.promise;
    }];

    /**
     * App routes
     */
    $stateProvider
      .state('home', {
        url: '/',
        controller: 'HomeCtrl as vm',
        templateUrl: require('./partials/home.html')
      })
      .state('login', {
        url: '/login',
        templateUrl: require('partials/login.html'),
        controller: 'LoginCtrl as vm',
        resolve: {
          skipIfLoggedIn
        }
      })
      .state('profile', {
        url: '/profile',
        templateUrl: require('partials/profile.html'),
        controller: 'ProfileCtrl as vm',
        resolve: {
          loginRequired
        }
      });
//    $urlRouterProvider.otherwise('/');

    /**
     *  Satellizer config
     */
    $authProvider.github({
      clientId: '91979aad542dfbe50b68',
      responseType: 'token'
    });

    $authProvider.oauth2({
      name: 'authserver',
      url: 'http://localhost:9999/uaa',
      clientId: 'acme',
      responseType: 'token',
      redirectUri: $windowProvider.$get().location.origin || `${$windowProvider.$get().location.protocol}//${$windowProvider.$get().location.host}`,
      authorizationEndpoint: 'http://localhost:9999/uaa/oauth/authorize'
    });
  });

class AppCtrl {
  constructor($auth, $location, toastr) {
    this.$auth = $auth;
    this.$location = $location;
    this.toastr = toastr;
  }

  isAuthenticated() {
    return this.$auth.isAuthenticated();
  }

  Logout() {
    if (!this.$auth.isAuthenticated()) { return; }
    this.$auth.logout()
      .then(() => {
        this.toastr.info('You have been logged out');
        this.$location.path('/');
      });
  }
}

AppCtrl.$inject = ['$auth', '$location', 'toastr'];
appModule.controller('AppCtrl', AppCtrl);
