class LoginCtrl {
  constructor($scope, $location, $auth, toastr) {
    this.$scope = $scope;
    this.$location = $location;
    this.$auth = $auth;
    this.toastr = toastr;
  }

  authenticate(provider) {
    this.$auth.authenticate(provider)
      .then(() => {
        this.toastr.success(`You have successfully signed in with ${provider}!`);
        this.$location.path('/');
      })
      .catch((error) => {
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
LoginCtrl.$inject = ['$scope', '$location', '$auth', 'toastr'];
export default LoginCtrl;
