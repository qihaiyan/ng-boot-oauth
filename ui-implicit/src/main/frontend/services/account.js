angular.module('MyApp')
  .factory('Account', function($http) {
    return {
      getProfile: function() {
        return $http.get('/user');
      }
    };
  });