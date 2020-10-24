/**
 * Sample React Native App with React Native In-App Update
 * Hooks version
 */

import React, { useEffect } from 'react';
import InAppUpdate from 'rn-in-app-update';

import { Alert } from 'react-native';
import AppComponent from './AppComponent'

const App = () => {

  useEffect(() => {
    const inAppUpdateConfig = {
      // after 10 days the new update will move from FLEXIBLE to IMMEDIATE
      staleDays: 10
    }

    // check for updates
    // if there is a new update it would display an update modal
    InAppUpdate.checkUpdate(inAppUpdateConfig);

    // update download completed
    // you either prompt your users to restart app     
    // or just show an alert of your choice

    // add callback, what has to be done when update is completed
    InAppUpdate.onUpdateDownloaded(() => {
      // update download completed
      // you either prompt your users to restart app     
      // or just show an alert of your choice

      console.log("Update successfully downloaded");

      Alert.alert('Update downloaded', 'New version was successfully downloaded, would you like to restart the app to update to the new version?', [
        {
          text: 'Restart now',
          onPress: () => {
            // force an update completion by restarting app
            InAppUpdate.forceCompleteUpdate();
          }
        },
        {
          text: 'Ok, do it later',
          onPress: () => console.log("Update later")
        }
      ]);

    });
  }, []);

  return <AppComponent />;
}

export default App;
