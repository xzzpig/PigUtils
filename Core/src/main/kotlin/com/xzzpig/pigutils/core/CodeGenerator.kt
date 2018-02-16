package com.xzzpig.pigutils.core


import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.util.*

/**
 * 随机验证码生成器
 */
class CodeGenerator {

    data class Code(val code: String, val img: BufferedImage)

    companion object {
        @JvmStatic
        inline fun build(block: CodeGenerator.() -> Unit): CodeGenerator = CodeGenerator().apply(block)
    }

    /**
     * 图片宽度
     */
    var width = 90
    /**
     * 图片高度
     */
    var height = 20
    /**
     * 验证码个数
     */
    var codeCount = 4// 定义图片上显示验证码的个数
    /**
     * 文字间隔(包含字符大小)
     */
    var xx = 15
    /**
     * 字符Y轴位置
     */
    var codeY = 16

    private val codeSequence by lazy { charArrayOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9') }
    /**
     * 字体
     */
    var font = Font("Fixedsys", Font.BOLD, 18)
    /**
     * 是否绘制边框
     */
    var border = true
    /**
     * 干扰线个数
     */
    var lineCount = 20
    /**
     * 背景色
     */
    var background = Color.WHITE

    val random = Random()
    /**
     * 字符生成器
     */
    var codeCreator = { random: Random ->
        codeSequence[random.nextInt(codeSequence.size)]
    }

    /**
     * 生成验证码
     * @return Code（验证码,验证码图片)
     */
    fun generate(): Code {
        // 定义图像buffer
        val buffImg = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        // Graphics2D gd = buffImg.createGraphics();
        // Graphics2D gd = (Graphics2D) buffImg.getGraphics();
        val gd = buffImg.graphics
        // 创建一个随机数生成器类
        // 将图像填充为白色
        gd.color = background
        gd.fillRect(0, 0, width, height)

        // 创建字体，字体的大小应该根据图片的高度来定。
        // 设置字体。
        gd.font = font

        if (border) {
            // 画边框。
            gd.color = Color.BLACK
            gd.drawRect(0, 0, width - 1, height - 1)
        }
        // 随机产生40条干扰线，使图象中的认证码不易被其它程序探测到。
        gd.color = Color.BLACK
        for (i in 1..lineCount) {
            val x = random.nextInt(width)
            val y = random.nextInt(height)
            val xl = random.nextInt(12)
            val yl = random.nextInt(12)
            gd.drawLine(x, y, x + xl, y + yl)
        }

        // randomCode用于保存随机产生的验证码，以便用户登录后进行验证。
        val randomCode = StringBuffer()
        var red = 0
        var green = 0
        var blue = 0

        // 随机产生codeCount数字的验证码。
        for (i in 0 until codeCount) {
            // 得到随机产生的验证码数字。
            val code = codeCreator(random).toString()
            // 产生随机的颜色分量来构造颜色值，这样输出的每位数字的颜色值都将不同。
            red = random.nextInt(255)
            green = random.nextInt(255)
            blue = random.nextInt(255)

            // 用随机产生的颜色将验证码绘制到图像中。
            gd.color = Color(red, green, blue)
            gd.drawString(code, (i + 1) * xx, codeY)

            // 将产生的四个随机数组合在一起。
            randomCode.append(code)
        }
        return Code(randomCode.toString(), buffImg)
    }
}