function SSEListener() {
    this.url = JPEXT.restUrl + '/broadcast';
    this.source = new EventSource(this.url);

    var that = this;
    this.source.onerror = function (event) {
        console.log("error [" + that.source.readyState + "]");
    };
    this.source.onopen = function (event) {
        console.log("eventsource opened!");
    };
    this.source.onmessage = function (event) {
        console.log(event.data);
        showSuccessFlag(event.data);
    };
}
