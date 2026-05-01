# プロジェクト構成

## server

サーバー側のコードを格納するディレクトリです.

`JabberServer` でクライアントからの接続を受け付け、適切な Service クラスを call します。
各 Service クラスは Request 引数を受け取って Request に応じた処理を行い、Response を返します。

```java
public class CreateRoomService {
    public CreateRoomResponse call(CreateRoomRequest request) {
        boolean success = roomRepository.create(request.roomId);
        return new CreateRoomResponse(success);
    }
}
```

```java
public class CreateRoomRequest {
    public String roomId;
}
```

```java
public class CreateRoomResponse {
    public boolean success;
}
```
