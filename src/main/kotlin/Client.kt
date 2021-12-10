import java.io.*
import java.lang.Exception
import java.net.Socket
import java.net.SocketException
import java.nio.file.Files

fun main(args: Array<String>) {
    val c = Client();
    c.start()
}

//C:/Users/Ilia/k.png
class Client {
    fun start() {
        println("format: [r | w] pathname")
        while (true) {
            val input = readLine()?.trim()?.replace("\\s+".toRegex(), " ")

            val splittedInput = input?.split(" ")
            if (splittedInput?.get(0) == "") {
                continue
            }

            if (splittedInput?.size == 1) {
                when ((splittedInput[0])) {
                    "exit" -> {
                        return
                    }
                    else -> {
                        println("format: [r | w] pathname")
                        continue
                    }
                }
            }
            if (splittedInput?.size == 2) {
                val inp0 = splittedInput[0]
                val inp1 = splittedInput[1]
                when (inp0) {
                    "r" -> {
                        readFile(inp1)
                        return
                    }
                    "w" -> {
                        writeFile(inp1)
                        return
                    }
                    else -> {
                        println("format: [r | w] pathname")
                        continue
                    }
                }

            }
            if (splittedInput?.size!! > 2) {
                println("format: [r | w] pathname")
                continue
            }

        }
    }

    private fun readFile(filePath: String) {
        val client = Socket("127.0.0.1", 9999)
        client.soTimeout = Utils.SOCKET_TIMEOUT

        val dataOutputStream = client.getOutputStream()
        val dataInputStream = client.getInputStream()

        val request = Utils.packRequest(Utils.Opcode.RRQ, filePath)
        dataOutputStream.write(request)

        val fileContent = ByteArrayOutputStream()
        var currentBlock: Short = 1
        var readBytes: Int
        while (true) {
            val input = ByteArray(Utils.PACKET_SIZE)
            try {
                readBytes = dataInputStream.read(input)
            } catch (e: SocketException) {
                TODO("Not yet implemented")
            }
            when (input[1].toShort()) {
                Utils.Opcode.Error.code -> {
                    TODO("Not yet implemented")
                }
                Utils.Opcode.Data.code -> {
                    val packData = Utils.unpackData(input)
                    if (currentBlock == packData.first) {
                        fileContent.write(input.copyOfRange(4, readBytes))
                        if (readBytes != Utils.PACKET_SIZE) {
                            break
                        }
                        currentBlock++
                    }
                    dataOutputStream.write(Utils.packACK(currentBlock))
                }
                else -> {
                    TODO("Not yet implemented")
                }
            }
        }
        client.close()
        val fileName = filePath.split('/').last()
        File(fileName).writeBytes(fileContent.toByteArray())
        println("File $fileName downloaded successfully.")
    }

    private fun writeFile(filePath: String) {
        val file: File
        val fileBytes: ByteArray
        try {
            file = File(filePath)
            fileBytes = Files.readAllBytes(file.toPath())
        } catch (e: Exception) {
            println("File not found.")
            return
        }

        val client = Socket("127.0.0.1", 9999)
        client.soTimeout = Utils.SOCKET_TIMEOUT

        val dataOutputStream = client.getOutputStream()
        val dataInputStream = client.getInputStream()

        val request = Utils.packRequest(Utils.Opcode.WRQ, filePath)
        dataOutputStream.write(request)

        var currentBlock: Short
        var input = ByteArray(Utils.PACKET_SIZE)
        try {
            dataInputStream.read(input)
        } catch (e: SocketException) {
            TODO("Not yet implemented")
        }
        if (input[1].toShort() == Utils.Opcode.ACK.code) {
            currentBlock = Utils.unpackACK(input)
        }
        else {
            println("error")
            return
        }

        while (true) {
            if (Utils.DATA_SIZE * (currentBlock+1) > fileBytes.size) {
                dataOutputStream.write(
                    Utils.packDataBlock(
                        currentBlock, fileBytes.copyOfRange(
                            Utils.DATA_SIZE * (currentBlock), fileBytes.size
                        )
                    )
                )
                break
            } else {
                dataOutputStream.write(
                    Utils.packDataBlock(
                        currentBlock, fileBytes.copyOfRange(
                            Utils.DATA_SIZE * (currentBlock), Utils.DATA_SIZE * (currentBlock+1)
                        )
                    )
                )
            }
            input = ByteArray(Utils.PACKET_SIZE)
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
        return
    }

    //ыаы ты думал тут что-то будет?)))000))да. Чел ты смишной. Аруу:))))
    private fun closeClient() {
        println(123)
        TODO("Not yet implemented")
    }

    private fun closeServer() {
        TODO("Not yet implemented")
    }
}