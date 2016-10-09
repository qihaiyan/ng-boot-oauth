class ProfileCtrl {
  constructor($scope, $auth, toastr, accountService) {
    this.$scope = $scope;
    this.$auth = $auth;
    this.toastr = toastr;
    this.accountService = accountService;
    console.log('get profile');
    this.accountService.getProfile()
      .then((response) => {
        this.user = response.data;
      })
      .catch((response) => {
        this.toastr.error(response.data.message, response.status);
      });
  }
}
ProfileCtrl.$inject = ['$scope', '$auth', 'toastr', 'accountService'];

export default ProfileCtrl;
