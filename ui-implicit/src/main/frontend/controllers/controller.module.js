import HomeCtrl from './home';
import LoginCtrl from './login';
import LogoutCtrl from './logout';
import NavbarCtrl from './navbar';
import ProfileCtrl from './profile';

let ctlModule = angular.module('CtlModule', []);

ctlModule.controller('HomeCtrl', HomeCtrl);
ctlModule.controller('LoginCtrl', HomeCtrl);
ctlModule.controller('LogoutCtrl', HomeCtrl);
ctlModule.controller('NavbarCtrl', HomeCtrl);
ctlModule.controller('ProfileCtrl', HomeCtrl);

export default ctlModule = ctlModule.name;