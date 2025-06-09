package com.taxiapp.driver_auth.configuration

import aws.sdk.kotlin.services.cognitoidentityprovider.CognitoIdentityProviderClient
import aws.sdk.kotlin.services.sns.SnsClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AwsConfig {
    @Value("\${aws.region}")
    private var awsRegion: String? = null

    @Bean
    open fun cognitoClient(): CognitoIdentityProviderClient {
        return CognitoIdentityProviderClient { region = awsRegion }
    }

    @Bean
    fun snsClient(): SnsClient {
        return SnsClient { region = awsRegion }
    }
}
