using uPLibrary.Networking.M2Mqtt;

namespace PappiBroker
{
    class Program
    {
        static void Main(string[] args)
        {
            var broker = new MqttBroker();
            broker.Start();
        }
    }
}
