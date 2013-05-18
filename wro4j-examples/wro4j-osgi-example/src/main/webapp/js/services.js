'use strict';

/* Services */


// Demonstrate how to register services
// In this case it is a simple value service.
angular.module('myApp.services', []).
  value('version', '0.1')

  .factory('StatusService', function(){
    this.lastError = {
      'class': 'alert-success',
      'message': 'All Systems Go'
    };
    return this.lastError;
  })

  //Service so controllers can communicate
  .factory('DataService', function(){
    this.data = {
    };
    return this.data;
  });
