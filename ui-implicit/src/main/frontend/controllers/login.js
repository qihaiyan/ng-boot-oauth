//angular.module('MyApp')
//  .controller('LoginCtrl', function($scope, $location, $auth, toastr) {
//    $scope.login = function() {
//      $auth.login($scope.user)
//        .then(function() {
//          toastr.success('You have successfully signed in!');
//          $location.path('/');
//        })
//        .catch(function(error) {
//          toastr.error(error.data.message, error.status);
//        });
//    };
//    $scope.authenticate = function(provider) {
//      $auth.authenticate(provider)
//        .then(function() {
//          toastr.success('You have successfully signed in with ' + provider + '!');
//          $location.path('/');
//          console.log($auth.isAuthenticated());
//        })
//        .catch(function(error) {
//          if (error.message) {
//            // Satellizer promise reject error.
//            toastr.error(error.message);
//          } else if (error.data) {
//            // HTTP response error from server
//            toastr.error(error.data.message, error.status);
//          } else {
//            toastr.error(error);
//          }
//        });
//    };
//  });

class LoginCtrl {
    constructor($scope, $location, $auth, toastr) {
    this.$scope = $scope;
    this.$location = $location;
    this.$auth = $auth;
    this.toastr = toastr;
    }

    login(){
    this.$auth.login(this.user)
            .then(function() {
              this.toastr.success('You have successfully signed in!');
              this.$location.path('/');
            })
            .catch(function(error) {
              this.toastr.error(error.data.message, error.status);
            });
    }

authenticate(provider){
this.$auth.authenticate(provider)
        .then(function() {
          this.toastr.success('You have successfully signed in with ' + provider + '!');
          this.$location.path('/');
          console.log(this.$auth.isAuthenticated());
        })
        .catch(function(error) {
          if (error.message) {
            // Satellizer promise reject error.
            this.toastr.error(error.message);
          } else if (error.data) {
            // HTTP response error from server
            this.toastr.error(error.data.message, error.status);
          } else {
            this.toastr.error(error);
          }
        });
}
}
LoginCtrl.$inject = [$scope, $location, $auth, toastr];
export default LoginCtrl;
