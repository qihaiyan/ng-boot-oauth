class AccountService {
  constructor($http) {
    this.$http = $http;
  }

  getProfile() {
    return this.$http.get('/user');
  }
}
AccountService.$inject = ['$http'];

let accountServiceModule = angular.module('AuthServiceModule', []);
accountServiceModule.service('accountService', AccountService);

export default accountServiceModule = accountServiceModule.name;
