package com.ojhdtapp.parabox.extension.ws.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ojhdtapp.parabox.extension.ws.MainActivity
import com.ojhdtapp.parabox.extension.ws.R
import com.ojhdtapp.parabox.extension.ws.core.util.BrowserUtil
import com.ojhdtapp.parabox.extension.ws.domain.util.ServiceStatus
import com.ojhdtapp.parabox.extension.ws.ui.util.NormalPreference
import com.ojhdtapp.parabox.extension.ws.ui.util.PreferencesCategory
import com.ojhdtapp.parabox.extension.ws.ui.util.SwitchPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {

    val context = LocalContext.current

    val isMainAppInstalled by viewModel.isMainAppInstalled.collectAsState()
    val serviceStatus by viewModel.serviceStatusStateFlow.collectAsState()

    // snackBar
    val snackBarHostState = remember { SnackbarHostState() }
    LaunchedEffect(true) {
        viewModel.uiEventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackBarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    // TopBar Scroll Behaviour
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var menuExpanded by remember {
        mutableStateOf(false)
    }

    var showEditUrlDialog by remember {
        mutableStateOf(false)
    }

    var showEditTokenDialog by remember {
        mutableStateOf(false)
    }

    val wsUrl = viewModel.wsUrlFlow.collectAsState(initial = "")
    val wsToken = viewModel.wsTokenFlow.collectAsState(initial = "")

    if (showEditUrlDialog) {
        var tempUrl by remember {
            mutableStateOf(wsUrl.value.split(":").getOrNull(0) ?: "")
        }
        var tempPort by remember {
            mutableStateOf(wsUrl.value.split(":").getOrNull(1) ?: "")
        }
        var editUrlError by remember {
            mutableStateOf(false)
        }
        var editPortError by remember {
            mutableStateOf(false)
        }
        AlertDialog(onDismissRequest = { showEditUrlDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    if (tempUrl.matches("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}\$".toRegex())
                        && tempPort.matches("\\d{1,5}".toRegex())
                    ) {
                        viewModel.setWSUrl(buildString {
                            append(tempUrl)
                            append(":")
                            append(tempPort)
                        })
                        showEditUrlDialog = false
                    } else {
                        if (!tempUrl.matches("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}\$".toRegex()))
                            editUrlError = true
                        if (!tempPort.matches("\\d{1,5}".toRegex()))
                            editPortError = true
                    }
                }) {
                    Text(text = stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditUrlDialog = false }) {
                    Text(text = stringResource(R.string.dismiss))
                }
            },
            title = {
                Text(text = stringResource(R.string.server_host))
            },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = "ws://",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = tempUrl,
                        onValueChange = {
                            editUrlError = false
                            tempUrl = it
                        },
                        isError = editUrlError,
                        label = { Text(text = stringResource(R.string.host)) },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = null
                        ),
                        singleLine = true,
                    )
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = ":",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    OutlinedTextField(
                        modifier = Modifier.width(80.dp),
                        value = tempPort,
                        onValueChange = {
                            editUrlError = false
                            tempPort = it
                        },
                        isError = editPortError,
                        label = { Text(text = stringResource(R.string.port)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = null
                        ),
                        singleLine = true,
                    )
                }
            }
        )
    }

    if(showEditTokenDialog){
        var tempToken by remember {
            mutableStateOf(wsToken.value)
        }
        var editTokenError by remember {
            mutableStateOf(false)
        }
        AlertDialog(onDismissRequest = { showEditTokenDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    if (tempToken.matches("^[a-zA-Z0-9]{1,32}\$".toRegex())) {
                        viewModel.setWSToken(tempToken)
                        showEditTokenDialog = false
                    } else {
                        editTokenError = true
                    }
                }) {
                    Text(text = stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditTokenDialog = false }) {
                    Text(text = stringResource(id = R.string.dismiss))
                }
            },
            title = {
                Text(text = stringResource(R.string.connection_token))
            },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = tempToken,
                        onValueChange = {
                            editTokenError = false
                            tempToken = it
                        },
                        isError = editTokenError,
                        label = { Text(text = "Token") },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = null
                        ),
                        singleLine = true,
                    )
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            LargeTopAppBar(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                title = { Text(text = stringResource(id = R.string.app_name)) },
                navigationIcon = {
                    if (isMainAppInstalled) {
                        IconButton(onClick = { (context as MainActivity).launchMainApp() }) {
                            Icon(
                                imageVector = Icons.Outlined.Home,
                                contentDescription = "back"
                            )
                        }
                    }
                },
                actions = {
                    Box() {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = "menu")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text(text = stringResource(id = R.string.force_stop_service)) },
                                onClick = {
                                    (context as MainActivity).forceStopParaboxService { }
                                    menuExpanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "stop service"
                                    )
                                })
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                MainSwitch(
                    textOff = stringResource(id = R.string.main_switch_off),
                    textOn = stringResource(id = R.string.main_switch_on),
                    checked = serviceStatus !is ServiceStatus.Stop && serviceStatus !is ServiceStatus.Error,
                    onCheckedChange = {

                        if (it) {
                            (context as MainActivity).startParaboxService {

                            }
                        } else {
                            (context as MainActivity).stopParaboxService {

                            }
                        }
                    },
                    enabled = serviceStatus is ServiceStatus.Stop || serviceStatus is ServiceStatus.Running || serviceStatus is ServiceStatus.Error
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                StatusIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    status = serviceStatus
                )
            }
            item(key = "info") {
                AnimatedVisibility(
                    visible = serviceStatus is ServiceStatus.Stop,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(modifier = Modifier.padding(24.dp, 16.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "info",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.extension_notice),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                PreferencesCategory(text = stringResource(id = R.string.action_category))
            }
            item {
                SwitchPreference(
                    title = stringResource(id = R.string.auto_login_title),
                    subtitle = stringResource(id = R.string.auto_login_subtitle),
                    checked = viewModel.autoLoginSwitchFlow.collectAsState(initial = false).value,
                    onCheckedChange = viewModel::setAutoLoginSwitch
                )
            }
            item {
                SwitchPreference(
                    title = stringResource(id = R.string.foreground_service_title),
                    subtitle = stringResource(id = R.string.foreground_service_subtitle),
                    checked = viewModel.foregroundServiceSwitchFlow.collectAsState(initial = true).value,
                    onCheckedChange = viewModel::setForegroundServiceSwitch
                )
            }
            item {
                SwitchPreference(
                    title = stringResource(R.string.auto_reconnect),
                    subtitle = stringResource(R.string.auto_reconnect_subtitle),
                    checked = viewModel.autoReconnectSwitchFlow.collectAsState(initial = true).value,
                    onCheckedChange = viewModel::setAutoReconnectSwitch
                )
            }
            item{
                NormalPreference(title = stringResource(id = R.string.server_host), subtitle = wsUrl.value.ifBlank { stringResource(
                                    R.string.not_set) }) {
                    showEditUrlDialog = true
                }
            }
            item{
                NormalPreference(title = stringResource(id = R.string.connection_token), subtitle = wsToken.value.ifBlank { stringResource(
                                    R.string.connection_token_subtitle) }) {
                    showEditTokenDialog = true
                }
            }
            item {
                PreferencesCategory(text = stringResource(R.string.about))
            }
            item {
                NormalPreference(
                    title = stringResource(R.string.version),
                    subtitle = viewModel.appVersion
                ) {
                    BrowserUtil.launchURL(
                        context,
                        "https://github.com/Parabox-App/parabox-extension-ws"
                    )
                }
                NormalPreference(
                    title = stringResource(R.string.user_guide),
                    subtitle = stringResource(R.string.user_guide_subtitle)
                ) {
                    BrowserUtil.launchURL(
                        context,
                        "https://github.com/Parabox-App/parabox-extension-ws"
                    )
                }
            }
        }
    }
}

@Composable
fun MainSwitch(
    modifier: Modifier = Modifier,
    textOff: String,
    textOn: String,
    checked: Boolean,
    onCheckedChange: (value: Boolean) -> Unit,
    enabled: Boolean
) {
    val switchColor by animateColorAsState(targetValue = if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(32.dp))
            .clickable {
                if (enabled) onCheckedChange(!checked)
            },
        color = switchColor,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp, 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (checked) textOn else textOff,
                style = MaterialTheme.typography.titleLarge,
                color = if (checked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        }
    }
}

@Composable
fun StatusIndicator(modifier: Modifier = Modifier, status: ServiceStatus) {
    AnimatedVisibility(
        visible = status !is ServiceStatus.Stop,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        val backgroundColor by animateColorAsState(
            targetValue = when (status) {
                is ServiceStatus.Error -> MaterialTheme.colorScheme.errorContainer
                is ServiceStatus.Loading -> MaterialTheme.colorScheme.primary
                is ServiceStatus.Running -> MaterialTheme.colorScheme.primary
                is ServiceStatus.Stop -> MaterialTheme.colorScheme.primary
                is ServiceStatus.Pause -> MaterialTheme.colorScheme.primary
            }
        )
        val textColor by animateColorAsState(
            targetValue = when (status) {
                is ServiceStatus.Error -> MaterialTheme.colorScheme.onErrorContainer
                is ServiceStatus.Loading -> MaterialTheme.colorScheme.onPrimary
                is ServiceStatus.Running -> MaterialTheme.colorScheme.onPrimary
                is ServiceStatus.Stop -> MaterialTheme.colorScheme.onPrimary
                is ServiceStatus.Pause -> MaterialTheme.colorScheme.onPrimary
            }
        )
        Row(modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(backgroundColor)
            .clickable { }
            .padding(24.dp, 24.dp),
            verticalAlignment = Alignment.CenterVertically) {
            when (status) {
                is ServiceStatus.Error -> Icon(
                    modifier = Modifier.padding(PaddingValues(end = 24.dp)),
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = "error",
                    tint = textColor
                )

                is ServiceStatus.Loading -> CircularProgressIndicator(
                    modifier = Modifier
                        .padding(PaddingValues(end = 24.dp))
                        .size(24.dp),
                    color = textColor,
                    strokeWidth = 3.dp
                )

                is ServiceStatus.Running -> Icon(
                    modifier = Modifier.padding(PaddingValues(end = 24.dp)),
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "running",
                    tint = textColor
                )

                is ServiceStatus.Stop -> Icon(
                    modifier = Modifier.padding(PaddingValues(end = 24.dp)),
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "stop",
                    tint = textColor
                )

                is ServiceStatus.Pause -> Icon(
                    modifier = Modifier.padding(PaddingValues(end = 24.dp)),
                    imageVector = Icons.Outlined.PauseCircleOutline,
                    contentDescription = "pause",
                    tint = textColor
                )
            }
            Column() {
                Text(
                    text = when (status) {
                        is ServiceStatus.Error -> stringResource(id = R.string.status_error)
                        is ServiceStatus.Loading -> stringResource(id = R.string.status_loading)
                        is ServiceStatus.Running -> stringResource(id = R.string.status_running)
                        is ServiceStatus.Stop -> ""
                        is ServiceStatus.Pause -> stringResource(id = R.string.status_pause)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = status.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
        }
    }
}