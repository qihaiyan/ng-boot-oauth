import HomeCtrl from './home';
import LoginCtrl from './login';
import ProfileCtrl from './profile';

let ctlModule = angular.module('CtlModule', []);

ctlModule.controller('HomeCtrl', HomeCtrl);
ctlModule.controller('LoginCtrl', LoginCtrl);
ctlModule.controller('ProfileCtrl', ProfileCtrl);

export default ctlModule = ctlModule.name;
