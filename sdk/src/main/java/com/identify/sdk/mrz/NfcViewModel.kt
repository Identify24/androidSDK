package com.identify.sdk.mrz

import android.content.Context
import android.graphics.Bitmap
import android.nfc.tech.IsoDep
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.identify.sdk.base.*
import com.identify.sdk.repository.model.BaseApiResponse
import com.identify.sdk.repository.model.CustomerInformationEntity
import com.identify.sdk.repository.model.dto.MrzDto
import com.identify.sdk.repository.model.dto.TanDto
import com.identify.sdk.repository.model.entities.TanEntity
import com.identify.sdk.repository.model.mrz.AdditionalPersonDetails
import com.identify.sdk.repository.model.mrz.DocType
import com.identify.sdk.repository.model.mrz.EDocument
import com.identify.sdk.repository.model.mrz.PersonDetails
import com.identify.sdk.repository.network.ApiImpl
import com.identify.sdk.util.DateUtil
import com.identify.sdk.util.Image
import com.identify.sdk.util.ImageUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.sf.scuba.smartcards.CardFileInputStream
import net.sf.scuba.smartcards.CardService
import org.jmrtd.BACKeySpec
import org.jmrtd.PassportService
import org.jmrtd.lds.CardSecurityFile
import org.jmrtd.lds.PACEInfo
import org.jmrtd.lds.icao.*
import org.jmrtd.lds.iso19794.FaceImageInfo
import org.jmrtd.lds.iso19794.FingerImageInfo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject

class NfcViewModel  : BaseViewModel<CustomerInformationEntity?>()  {

    var eDocument: EDocument = EDocument()
    var errorMrz  = MutableLiveData<Reason>()
    var docType: DocType = DocType.OTHER
    var personDetails: PersonDetails = PersonDetails()
    var additionalPersonDetails: AdditionalPersonDetails = AdditionalPersonDetails()
    var mrzInfo : MRZInfo ?= null
    var customer : CustomerInformationEntity ?= null
    private var context : Context ?= null

    val nfcApiResult = MutableLiveData<Boolean>()

    private val apiImpl : ApiImpl by lazy {
        ApiImpl()
    }



    fun initSources(context: Context){
        this.context = context
    }


    fun getNfcData(isoDep : IsoDep, bacKey : BACKeySpec){
      viewModelScope.launch {
          withContext(Dispatchers.IO){
              try {
                  val cardService = CardService.getInstance(isoDep)
                  cardService.open()
                  val service = PassportService(
                      cardService,
                      PassportService.NORMAL_MAX_TRANCEIVE_LENGTH,
                      PassportService.DEFAULT_MAX_BLOCKSIZE,
                      true,
                      false
                  )
                  service.open()
                  var paceSucceeded = false
                  try {
                      val cardSecurityFile : CardSecurityFile = CardSecurityFile(service.getInputStream(PassportService.EF_CARD_SECURITY))
                      val securityInfoCollection =
                          cardSecurityFile.securityInfos
                      for (securityInfo in securityInfoCollection) {
                          if (securityInfo is PACEInfo) {
                              val paceInfo = securityInfo
                              service.doPACE(
                                  bacKey,
                                  paceInfo.objectIdentifier,
                                  PACEInfo.toParameterSpec(paceInfo.parameterId),
                                  null
                              )
                              paceSucceeded = true
                          }
                      }
                  } catch (e: Exception) {
                  }
                  service.sendSelectApplet(paceSucceeded)
                  if (!paceSucceeded) {
                      try {
                          service.getInputStream(PassportService.EF_COM).read()
                      } catch (e: Exception) {
                          service.doBAC(bacKey)
                      }
                  }

                  // -- Personal Details -- //

                  // -- Personal Details -- //
                  val dg1In : CardFileInputStream = service.getInputStream(PassportService.EF_DG1)
                  val dg1File = DG1File(dg1In)
                  val mrzInfo = dg1File.mrzInfo
                  personDetails.name = mrzInfo.secondaryIdentifier.replace("<", " ").trim { it <= ' ' }

                  personDetails.surname = mrzInfo.primaryIdentifier.replace("<", " ").trim { it <= ' ' }
                  personDetails.personalNumber = mrzInfo.personalNumber
                  personDetails.gender = mrzInfo.gender.toString()
                  personDetails.birthDate = DateUtil.convertFromMrzDate(mrzInfo.dateOfBirth)
                  personDetails.expiryDate = DateUtil.convertFromMrzDate(mrzInfo.dateOfExpiry)
                  personDetails.serialNumber = mrzInfo.documentNumber
                  personDetails.nationality = mrzInfo.nationality
                  personDetails.issuerAuthority = mrzInfo.issuingState
                  if ("I" == mrzInfo.documentCode) {
                      docType = DocType.ID_CARD
                  } else if ("P" == mrzInfo.documentCode) {
                      docType = DocType.PASSPORT
                  }

                  // -- Face Image -- //
                  val dg2In = service.getInputStream(PassportService.EF_DG2)
                  val dg2File = DG2File(dg2In)
                  val faceInfos = dg2File.faceInfos
                  val allFaceImageInfos: MutableList<FaceImageInfo> =
                      ArrayList()
                  for (faceInfo in faceInfos) {
                      allFaceImageInfos.addAll(faceInfo.faceImageInfos)
                  }
                  if (!allFaceImageInfos.isEmpty()) {
                      val faceImageInfo = allFaceImageInfos.iterator().next()
                      val image: Image = ImageUtil.getImage(context, faceImageInfo)
                      personDetails.faceImage = image.bitmapImage
                      personDetails.faceImageBase64 = image.base64Image
                  }

                  // -- Fingerprint (if exist)-- //
                  try {
                      val dg3In = service.getInputStream(PassportService.EF_DG3)
                      val dg3File = DG3File(dg3In)
                      val fingerInfos = dg3File.fingerInfos
                      val allFingerImageInfos: MutableList<FingerImageInfo> =
                          ArrayList()
                      for (fingerInfo in fingerInfos) {
                          allFingerImageInfos.addAll(fingerInfo.fingerImageInfos)
                      }
                      val fingerprintsImage: MutableList<Bitmap> =
                          ArrayList()
                      if (!allFingerImageInfos.isEmpty()) {
                          for (fingerImageInfo in allFingerImageInfos) {
                              val image: Image? = ImageUtil.getImage(context, fingerImageInfo)
                              image?.let {
                                  fingerprintsImage.add(image.bitmapImage)
                              }
                          }
                          personDetails.fingerprints = fingerprintsImage
                      }
                  } catch (e: Exception) {
                  }

                  // -- Portrait Picture -- //
                  try {
                      val dg5In = service.getInputStream(PassportService.EF_DG5)
                      val dg5File = DG5File(dg5In)
                      val displayedImageInfos =
                          dg5File.images
                      if (!displayedImageInfos.isEmpty()) {
                          val displayedImageInfo =
                              displayedImageInfos.iterator().next()
                          val image: Image = ImageUtil.getImage(context, displayedImageInfo)
                          personDetails.portraitImage = image.bitmapImage
                          personDetails.portraitImageBase64 = image.base64Image
                      }
                  } catch (e: Exception) {
                  }

                  // -- Signature (if exist) -- //
                  try {
                      val dg7In = service.getInputStream(PassportService.EF_DG7)
                      val dg7File = DG7File(dg7In)
                      val signatureImageInfos =
                          dg7File.images
                      if (!signatureImageInfos.isEmpty()) {
                          val displayedImageInfo =
                              signatureImageInfos.iterator().next()
                          val image: Image = ImageUtil.getImage(context, displayedImageInfo)
                          personDetails.portraitImage = image.bitmapImage
                          personDetails.portraitImageBase64 = image.base64Image
                      }
                  } catch (e: Exception) {
                  }

                  // -- Additional Details (if exist) -- //
                  try {
                      val dg11In = service.getInputStream(PassportService.EF_DG11)
                      val dg11File = DG11File(dg11In)
                      if (dg11File.length > 0) {
                          additionalPersonDetails.custodyInformation = dg11File.custodyInformation
                          additionalPersonDetails.nameOfHolder = dg11File.nameOfHolder
                          additionalPersonDetails.fullDateOfBirth = dg11File.fullDateOfBirth
                          additionalPersonDetails.otherNames = dg11File.otherNames
                          additionalPersonDetails.otherValidTDNumbers = dg11File.otherValidTDNumbers
                          additionalPersonDetails.permanentAddress = dg11File.permanentAddress
                          additionalPersonDetails.personalNumber = dg11File.personalNumber
                          additionalPersonDetails.personalSummary = dg11File.personalSummary
                          additionalPersonDetails.placeOfBirth = dg11File.placeOfBirth
                          additionalPersonDetails.profession = dg11File.profession
                          additionalPersonDetails.proofOfCitizenship  = dg11File.proofOfCitizenship
                          additionalPersonDetails.tag = dg11File.tag
                          additionalPersonDetails.tagPresenceList = dg11File.tagPresenceList
                          additionalPersonDetails.telephone = dg11File.telephone
                          additionalPersonDetails.title = dg11File.title
                      }
                  } catch (e: Exception) {
                  }

                  // -- Document Public Key -- //
                  try {
                      val dg15In = service.getInputStream(PassportService.EF_DG15)
                      val dg15File = DG15File(dg15In)
                      val publicKey = dg15File.publicKey
                      eDocument.docPublicKey = publicKey
                  } catch (e: Exception) {
                  }
                  eDocument.docType = docType
                  eDocument.personDetails = personDetails
                  eDocument.additionalPersonDetails = additionalPersonDetails
                  setMrzData()
              } catch (e: Exception) {
                  handleMrzError(NfcError())
              }
          }
      }
    }
    private fun setMrzData() {
        viewModelScope.launch {

            handleState(State.Loading())
            apiImpl.service.setMrzData(MrzDto(eDocument.personDetails?.issuerAuthority,eDocument.personDetails?.birthDate,eDocument.docType?.name,eDocument.personDetails?.expiryDate,eDocument.personDetails?.gender,customer?.formUid,eDocument.personDetails?.name,eDocument.personDetails?.nationality,eDocument.personDetails?.personalNumber,eDocument.personDetails?.serialNumber,eDocument.personDetails?.surname,eDocument.personDetails?.faceImageBase64))?.enqueue( object :
                Callback<BaseApiResponse<CustomerInformationEntity?>> {
                override fun onFailure(
                    call: Call<BaseApiResponse<CustomerInformationEntity?>>,
                    t: Throwable
                ) {
                    t.message?.let { handleFailure(ResponseError()) }
                    handleState(State.Loaded())
                }

                override fun onResponse(
                    call: Call<BaseApiResponse<CustomerInformationEntity?>>,
                    response: Response<BaseApiResponse<CustomerInformationEntity?>>
                ) {
                    if (response.isSuccessful && response.body()?.result == true){
                        response.body()?.result.let {
                            nfcApiResult.value = it
                        }
                    }else{
                        if (response.body()?.messages?.get(0).isNullOrEmpty()) handleFailure(
                            ResponseError()
                        ) else handleFailure(ApiError(response.body()?.messages))

                    }
                    handleState(State.Loaded())
                }

            })
        }
    }

    private fun handleMrzError(error: Reason){
        errorMrz.postValue(error)
    }

}