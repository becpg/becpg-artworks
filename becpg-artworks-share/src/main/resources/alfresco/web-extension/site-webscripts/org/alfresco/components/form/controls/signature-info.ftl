<#macro renderDate rawDate>
   <#if rawDate?has_content>
      <#assign parsedDate = "">
      <#assign savedLocale = .locale>
      <#if rawDate?matches("^\\d{4}-\\d{2}-\\d{2}.*")>
         <#attempt>
            <#assign parsedDate = xmldate(rawDate)>
         <#recover>
            <#assign parsedDate = "">
         </#attempt>
      <#elseif rawDate?matches("^[A-Za-z]{3}.*")>
         <#attempt>
            <#setting locale="en_US">
            <#assign parsedDate = rawDate?datetime("EEE MMM dd HH:mm:ss z yyyy")>
         <#recover>
            <#assign parsedDate = "">
         </#attempt>
         <#setting locale=savedLocale>
      </#if>
      
      <#if parsedDate?has_content && (parsedDate?is_date || parsedDate?is_date_like)>
         <#setting locale=savedLocale>
         <#assign isoDate = parsedDate?string("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")>
         <#assign viewFormat>${msg("form.control.date-picker.view.time.format")!"EEE d MMM yyyy HH:mm:ss"}</#assign>
         <span class="viewmode-value viewmode-value-date" data-date-iso8601="${isoDate}" data-show-time="true">${parsedDate?string(viewFormat)}</span>
      <#else>
         <#setting locale=savedLocale>
         <span class="viewmode-value viewmode-value-date" data-date-iso8601="${rawDate}" data-show-time="true">${rawDate?html}</span>
      </#if>
   </#if>
</#macro>

<#assign controlId = fieldHtmlId + "-cntrl">
<#assign fieldValue = field.value!"">
<div class="form-field">
   <#if form.mode == "view">
      <div id="${controlId}" class="viewmode-field">
         <span class="viewmode-label">${field.label?html}:</span>
         <#if fieldValue?has_content>
            <#attempt>
               <#assign signatureData = fieldValue?eval>
               <#assign isValid = true>
            <#recover>
               <#assign isValid = false>
            </#attempt>
            <#if isValid && signatureData?? && signatureData.recipients??>
               <div class="viewmode-value signature-info-control">
                  <table class="signature-info-table" style="width: 100%; border-collapse: collapse; margin-top: 5px;">
                     <thead>
                        <tr style="background-color: #f4f4f4; text-align: left; border-bottom: 1px solid #ccc;">
                           <th style="padding: 6px;">${msg("label.signature.username")!"Signataire"}</th>
                           <th style="padding: 6px;">${msg("label.signature.preparationDate")!"Date de préparation"}</th>
                           <th style="padding: 6px;">${msg("label.signature.signatureDate")!"Date de signature"}</th>
                        </tr>
                     </thead>
                     <tbody>
                        <#list signatureData.recipients as recipient>
                           <tr style="border-bottom: 1px solid #eee;">
                              <td style="padding: 6px;">${(recipient.username!"")?html}</td>
                              <td style="padding: 6px;"><@renderDate recipient.preparationDate!"" /></td>
                              <td style="padding: 6px;"><@renderDate recipient.signatureDate!"" /></td>
                           </tr>
                        </#list>
                     </tbody>
                  </table>
               </div>
            <#else>
               <span class="viewmode-value">${fieldValue?html}</span>
            </#if>
         </#if>
      </div>
   <#else>
      <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
      <input type="hidden" id="${fieldHtmlId}" name="${field.name}" value="${fieldValue?html}" />
      <#if fieldValue?has_content>
         <#attempt>
            <#assign signatureData = fieldValue?eval>
            <#assign isValid = true>
         <#recover>
            <#assign isValid = false>
         </#attempt>
         <#if isValid && signatureData?? && signatureData.recipients??>
            <div id="${controlId}" class="signature-info-control">
               <table class="signature-info-table" style="width: 100%; border-collapse: collapse; margin-top: 5px;">
                  <thead>
                     <tr style="background-color: #f4f4f4; text-align: left; border-bottom: 1px solid #ccc;">
                        <th style="padding: 6px;">${msg("label.signature.username")!"Signataire"}</th>
                        <th style="padding: 6px;">${msg("label.signature.preparationDate")!"Date de préparation"}</th>
                        <th style="padding: 6px;">${msg("label.signature.signatureDate")!"Date de signature"}</th>
                     </tr>
                  </thead>
                  <tbody>
                     <#list signatureData.recipients as recipient>
                        <tr style="border-bottom: 1px solid #eee;">
                           <td style="padding: 6px;">${(recipient.username!"")?html}</td>
                           <td style="padding: 6px;"><@renderDate recipient.preparationDate!"" /></td>
                           <td style="padding: 6px;"><@renderDate recipient.signatureDate!"" /></td>
                        </tr>
                     </#list>
                  </tbody>
               </table>
            </div>
         <#else>
            <span class="viewmode-value">${fieldValue?html}</span>
         </#if>
      </#if>
   </#if>
</div>
