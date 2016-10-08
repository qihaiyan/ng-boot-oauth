import HomeCtrl from './home';
import LoginCtrl from './login';
import LogoutCtrl from './logout';
import ProfileCtrl from './profile';

let ctlModule = angular.module('CtlModule', []);

ctlModule.controller('HomeCtrl', HomeCtrl);
ctlModule.controller('LoginCtrl', LoginCtrl);
ctlModule.controller('LogoutCtrl', LogoutCtrl);
ctlModule.controller('ProfileCtrl', ProfileCtrl);

export default ctlModule = ctlModule.name;
