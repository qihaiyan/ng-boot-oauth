import angular from 'angular';
import ngMessages from 'angular-messages';
import ngResource from 'angular-resource';
import ngAnimate from 'angular-animate';
import toastr from 'angular-toastr';
import ui.router from 'angular-ui-router';
import satellizer from 'angular-satellizer';
import accountServiceModule from './services/account';
import homeModule from './controllers/home';
import loginModule from './controllers/login';
import logoutModule from './controllers/logout';
import navbarModule from './controllers/navbar';
import profileModule from './controllers/profile';

const appModule = angular.module('MyApp', [
  ngMessages,
  ngResource,
  ngAnimate,
  toastr,
  ui.router,
  satellizer,
  accountServiceModule,
  homeModule,
  loginModule,
  logoutModule,
  navbarModule,
  profileModule,
]);

export default appModule;

appModule.config(function($stateProvider, $urlRouterProvider, $authProvider) {

    /**
     * Helper auth functions
     */
    const skipIfLoggedIn = ['$q', '$auth', function($q, $auth) {
      const deferred = $q.defer();
      if ($auth.isAuthenticated()) {
        deferred.reject();
      } else {
        deferred.resolve();
      }
      return deferred.promise;
    }];

    const loginRequired = ['$q', '$location', '$auth', function($q, $location, $auth) {
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
        controller: 'HomeCtrl',
        templateUrl: 'partials/home.html'
      })
      .state('login', {
        url: '/login',
        templateUrl: 'partials/login.html',
        controller: 'LoginCtrl',
        resolve: {
          skipIfLoggedIn: skipIfLoggedIn
        }
      })
      .state('logout', {
        url: '/logout',
        template: null,
        controller: 'LogoutCtrl'
      })
      .state('profile', {
        url: '/profile',
        templateUrl: 'partials/profile.html',
        controller: 'ProfileCtrl',
        resolve: {
          loginRequired: loginRequired
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
      redirectUri: window.location.origin || window.location.protocol + '//' + window.location.host,
      authorizationEndpoint: 'http://localhost:9999/uaa/oauth/authorize'
    });
  });
