import jdk.jshell.execution.Util
import java.io.*
import java.lang.Exception
import java.net.*
import java.nio.file.Files


fun main(args: Array<String>) {
    val s = Server();
    s.start()
}

class Server {
    fun start() {
        val server = ServerSocket(9999)
        val client = server.accept()

        val input = ByteArray(Utils.PACKET_SIZE)
        client.getInputStream().read(input)
        val request = Utils.unpackRequest(input)
        if (request.first == Utils.Opcode.RRQ) readMode(request, client)
        if (request.first == Utils.Opcode.WRQ) writeMode(request, client)

    }

    private fun writeMode(request: Pair<Utils.Opcode, List<String>>, client: Socket) {
        val dataInputStream = DataInputStream(client.getInputStream())
        val dataOutputStream = DataOutputStream(client.getOutputStream())

        val byteStream = ByteArrayOutputStream()
        var currentBlock: Short = 0

        val toWrite = Utils.packACK(currentBlock)
        dataOutputStream.writeInt(toWrite.size)
        dataOutputStream.write(toWrite)

        while (true) {
            val arraySize = dataInputStream.readInt();
            val input = ByteArray(arraySize)
            dataInputStream.read(input)

            val inputOnlyData = ByteArray(arraySize - 4)
            System.arraycopy(input, 4, inputOnlyData, 0, arraySize - 4);
            byteStream.write(inputOnlyData)

            val packData = Utils.unpackData(input)
            currentBlock++
            if (packData.second.size < 512) {

                val fileName = request.second[0].split('/').last()
                File(fileName).writeBytes(byteStream.toByteArray())
                println("File $fileName downloaded successfully.")
                return
            }
            val toWrite = Utils.packACK(currentBlock)
            dataOutputStream.writeInt(toWrite.size)
            dataOutputStream.write(toWrite)

        }
    }

    private fun readMode(request: Pair<Utils.Opcode, List<String>>, client: Socket) {
        client.soTimeout = Utils.SOCKET_TIMEOUT
        val dataInputStream = DataInputStream(client.getInputStream())
        val dataOutputStream = DataOutputStream(client.getOutputStream())

        val file: File
        var fileBytes = ByteArray(0)
        try {
            file = File(request.second[0])
            fileBytes = Files.readAllBytes(file.toPath())
        } catch (e: Exception) {
            //dataOutputStream.write(Utils.packError())
            println("RRQ from ${client.localAddress}: File not found.") //ну так себе выводит
        }
        var currentBlock: Short = 1
        while (true) {
            if (Utils.DATA_SIZE > fileBytes.size - Utils.DATA_SIZE * (currentBlock - 1)) {
                dataOutputStream.write(
                    Utils.packDataBlock(
                        currentBlock, fileBytes.copyOfRange(Utils.DATA_SIZE * (currentBlock - 1), fileBytes.size)
                    )
                )
                break
            } else {
                dataOutputStream.write(
                    Utils.packDataBlock(
                        currentBlock, fileBytes.copyOfRange(
                            Utils.DATA_SIZE * (currentBlock - 1), Utils.DATA_SIZE * currentBlock
                        )
                    )
                )
            }
            val input = ByteArray(Utils.PACKET_SIZE)
            try {
                dataInputStream.read(input)
            } catch (e: SocketException) {
                TODO("Not yet implemented")
            }
            when (input[1].toShort()) {
                Utils.Opcode.ACK.code -> {
                    currentBlock = Utils.unpackACK(input)
                }
                else -> {
                    TODO("Not yet implemented")
                }
            }
        }
    }
}