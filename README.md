# stream-android

StreamServer - Waits for StreamClients to connect. When a client is connected, the client can send m3u8-links, that contain a video-stream. When it is a valid m3u8-link, the video is being played.

StreamClient - Can connect to a StreamServer. When connected the StreamClient can send a link to the StreamServer. Otherwise the video is being played on the StreamClient. The links provided can either be m3u8-links directly or http-links that contain m3u8-links, when requested.

Ip and port can be changed, when looking in the source-code.