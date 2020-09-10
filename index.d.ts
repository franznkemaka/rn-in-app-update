declare module 'react-native-in-app-update' {
    interface InAppUpdate {
        /**
         * Check for updates
         * with configurations
         */
        checkUpdate(config: UpdatesConfigObject): void;

        /**
         * Fires when the update is in FLEXIBLE mode and
         * the download is completed
         *
         * Use the callback function to show a custom alert
         */
        onUpdateDownloaded(callback: CallableFunction): void;

        /**
         * Terminates the update process
         *
         * It is advised to fire it in the callback function of 'onUpdateDownloaded'
         */
        forceCompleteUpdate(): void;
    }

    let InAppUpdate: InAppUpdate;
    export default InAppUpdate;
}

type UpdatesConfigObject = {
    /**
     * Days after which the update is set from FLEXIBLE to IMMEDIATE mode
     * @default 5
     */
    staleDays?: number;
};
