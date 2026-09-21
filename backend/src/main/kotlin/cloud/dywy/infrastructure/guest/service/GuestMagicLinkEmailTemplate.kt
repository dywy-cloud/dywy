package cloud.dywy.infrastructure.guest.service

import cloud.dywy.domain.guest.entity.Language
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component
import org.springframework.web.util.HtmlUtils.htmlEscape
import java.util.Locale

private const val ICON_CID_SRC = "cid:${GuestMagicLinkEmailAssets.ICON_CONTENT_ID}"

@Component
class GuestMagicLinkEmailTemplate(
    private val messages: MessageSource,
) {


    fun subject(language: Language, coupleDisplayName: String) =
        message("email.magic-link.subject", language.toLocale(), coupleDisplayName)

    fun textBody(guestFirstName: String, magicLinkUrl: String, language: Language): String {
        val locale = language.toLocale()
        return """
            ${message("email.magic-link.greeting", locale, guestFirstName)}

            ${message("email.magic-link.intro", locale)}
            $magicLinkUrl

            ${message("email.magic-link.ignore", locale)}

            --
            ${message("email.magic-link.signature", locale)}
        """.trimIndent()
    }

    fun htmlBody(
        guestFirstName: String,
        magicLinkUrl: String,
        language: Language,
        coupleDisplayName: String,
        iconSrc: String = ICON_CID_SRC,
    ): String {
        val locale = language.toLocale()
        return htmlTemplate(
            lang = locale.language,
            greeting = message("email.magic-link.greeting", locale, htmlEscape(guestFirstName)),
            intro = message("email.magic-link.intro", locale),
            cta = message("email.magic-link.cta", locale),
            fallback = message("email.magic-link.fallback", locale),
            ignore = message("email.magic-link.ignore", locale),
            signature = message("email.magic-link.signature", locale),
            coupleDisplayName = htmlEscape(coupleDisplayName),
            iconSrc = htmlEscape(iconSrc),
            magicLinkUrl = magicLinkUrl,
        )
    }

    private fun message(code: String, locale: Locale, vararg args: Any): String =
        messages.getMessage(code, args.takeIf { it.isNotEmpty() }, locale)

    private fun htmlTemplate(
        lang: String,
        greeting: String,
        intro: String,
        cta: String,
        fallback: String,
        ignore: String,
        signature: String,
        coupleDisplayName: String,
        iconSrc: String,
        magicLinkUrl: String,
    ) = """
        <!doctype html>
        <html lang="$lang">
          <body style="margin:0;padding:0;background:#f4f5f5;font-family:Arial,sans-serif;color:#37474f;">
            <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="padding:24px;">
              <tr>
                <td align="center">
                  <table role="presentation" width="600" cellspacing="0" cellpadding="0" style="background:#ffffff;border-radius:10px;padding:16px 24px 24px;box-shadow:0 2px 8px rgba(55,71,79,0.1);">
                    <tr>
                      <td align="left" style="padding-bottom:8px;">
                        <img src="$iconSrc" alt="dywy" width="40" height="40" style="display:block;height:40px;width:40px;border-radius:10px;" />
                      </td>
                    </tr>
                    <tr>
                      <td style="border-top:1px solid #e79aae;"></td>
                    </tr>
                    <tr><td style="height:18px;"></td></tr>
                    <tr>
                      <td style="text-align:center;font-size:16px;">
                        <span style="color:#37474f;font-weight:600;">$coupleDisplayName</span>
                      </td>
                    </tr>
                    <tr>
                      <td style="height:18px;"></td>
                    </tr>
                    <tr>
                      <td style="border-top:1px solid #bac2bd;"></td>
                    </tr>
                    <tr><td style="height:24px;"></td></tr>
                    <tr>
                      <td style="font-size:16px;line-height:1.5;">
                        <span style="color:#e79aae;font-weight:600;">$greeting</span><br/><br/>
                        $intro
                      </td>
                    </tr>
                    <tr><td style="height:24px;"></td></tr>
                    <tr>
                      <td align="center">
                        <a href="${htmlEscape(magicLinkUrl)}" style="display:inline-block;background:#37474f;background:linear-gradient(135deg, #788f9c 0%, #37474f 100%);color:#ffffff;text-decoration:none;padding:12px 24px;border-radius:8px;font-weight:600;">
                          $cta
                        </a>
                      </td>
                    </tr>
                    <tr><td style="height:20px;"></td></tr>
                    <tr>
                      <td style="font-size:13px;color:#37474f;line-height:1.5;">
                        $fallback<br/>
                        <a href="${htmlEscape(magicLinkUrl)}" style="color:#37474f;word-break:break-all;">${htmlEscape(magicLinkUrl)}</a>
                      </td>
                    </tr>
                    <tr><td style="height:12px;"></td></tr>
                    <tr>
                      <td style="font-size:13px;color:#37474f;line-height:1.5;">
                        $ignore
                      </td>
                    </tr>
                    <tr><td style="height:20px;"></td></tr>
                    <tr>
                      <td style="font-size:13px;color:#37474f;">$signature</td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
          </body>
        </html>
    """.trimIndent()
}


