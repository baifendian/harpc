#file  echo.thrift
namespace cpp bfd.harpc.demo
service EchoService {
   string echo(1:string msg)
}