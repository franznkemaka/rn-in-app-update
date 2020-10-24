const RNInAppUpdate = {
    checkUpdate: () => platformNotCurrentlySupported(),
    onUpdateDownloaded: (callback) => platformNotCurrentlySupported(),
    forceCompleteUpdate: () => platformNotCurrentlySupported()
}

const platformNotCurrentlySupported = () => console.warn("RNInAppUpdate doesn't support iOS at the moment");

export default RNInAppUpdate;