package com.moyarossiya.tv.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.moyarossiya.tv.R
import com.moyarossiya.tv.ui.theme.MoyaCard
import com.moyarossiya.tv.ui.theme.MoyaGold
import com.moyarossiya.tv.ui.theme.MoyaText

@Composable
fun AboutScreen(compact: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(if (compact) Modifier.verticalScroll(rememberScrollState()) else Modifier)
            .padding(
                horizontal = if (compact) 16.dp else 48.dp,
                vertical = if (compact) 16.dp else 32.dp,
            ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = androidx.compose.ui.res.stringResource(R.string.logo_description),
                modifier = Modifier.size(if (compact) 76.dp else 112.dp),
            )
            Spacer(modifier = Modifier.width(if (compact) 16.dp else 28.dp))
            Column {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.app_name),
                    color = MoyaText,
                    style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.about_tagline),
                    color = MoyaGold,
                    style = if (compact) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleMedium,
                )
            }
        }
        Spacer(modifier = Modifier.height(if (compact) 20.dp else 28.dp))
        Text(
            text = androidx.compose.ui.res.stringResource(R.string.about_description),
            color = MoyaText,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(if (compact) 20.dp else 28.dp))
        if (compact) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoCard(
                    title = androidx.compose.ui.res.stringResource(R.string.about_devices_title),
                    text = androidx.compose.ui.res.stringResource(R.string.about_devices_text),
                    modifier = Modifier.fillMaxWidth(),
                )
                InfoCard(
                    title = androidx.compose.ui.res.stringResource(R.string.about_internet_title),
                    text = androidx.compose.ui.res.stringResource(R.string.about_internet_text),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                InfoCard(
                    title = androidx.compose.ui.res.stringResource(R.string.about_devices_title),
                    text = androidx.compose.ui.res.stringResource(R.string.about_devices_text),
                    modifier = Modifier.weight(1f),
                )
                InfoCard(
                    title = androidx.compose.ui.res.stringResource(R.string.about_internet_title),
                    text = androidx.compose.ui.res.stringResource(R.string.about_internet_text),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(modifier = Modifier.height(22.dp))
        MutedText(androidx.compose.ui.res.stringResource(R.string.about_footer))
    }
}

@Composable
private fun InfoCard(title: String, text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MoyaCard,
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = title, color = MoyaGold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = text, color = MoyaText, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
