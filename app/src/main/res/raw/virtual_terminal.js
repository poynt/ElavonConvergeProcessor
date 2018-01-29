if (window.self !== window.top) {
  window.top.location = window.self.location;
}

$(function () {
  var currency = $('#vt-form-currency').val() || 'USD';
  var cardPresent = false; // only used for elavon
  var ACTION = 'sale';

  var submitButton = $('#vt-form-submit');
  var amountForm = $('.sale-form');
  var refundForm = $('#vt-form-amount-refund');
  var refundInput = $('#vt-refund-form-amount');
  var enabledRefund = false;
  var saleMode = true;
  var dccData; // for dcc exchange rates info

  // decorators for card/expiration/cvc/zip formats
  $('#vt-form-card').payment('formatCardNumber');
  $('#vt-form-exp').payment('formatCardExpiry');
  $('#vt-form-cvc').payment('formatCardCVC');

  FastClick.attach(document.body);

  // for elavon token
  var calculateLRC = function calculateLRC(str) {
    /* jshint ignore:start */
    var bytes = [];
    var lrc = 0;
    for (var i = 0; i < str.length; i++) {
      bytes.push(str.charCodeAt(i));
    }
    for (i = 1; i < str.length; i++) {
      lrc ^= bytes[i];
    }
    return String.fromCharCode(lrc);
    /* jshint ignore:end */
  };

  /**
   * Parses elavon token
   * @param {String} body
   * @return {String} token
   */
  var parseElavonToken = function (body) {
    var token = '';
    var count = 0; // looking for GS, 38 and 45 consecutive

    // for loop to extract the token
    for (var i = 0; i < body.length; i++) {
      var char = body.charCodeAt(i).toString(16);

      if (char === '1d' && count === 0) {
        count = count + 1;
      } else if (char === '38' && count === 1) {
        count = count + 1;
      } else if (char === '45' && count === 2) {
        count = count + 1;
      } else if (count === 3) {
        if (char === '3') {
          count = 0;
        } else {
          token = token + char[1];
        }
      } else {
        count = 0;
      }
    }

    return token;
  };

  var TEST_ELAVON_URL = 'https://certgate.viaconex.com/cgi-bin/encompass.cgi';
  var PROD_ELAVON_URL = 'https://prodgate.viaconex.com/cgi-bin/encompass.cgi';

  var DISPLAY_ERROR_FLAG = 'DISPLAY_ERROR';

  var getElavonToken = function getElavonToken(registrationKey, bodyString, next) {
    const callbackId = (new Date()).valueOf() + '';

    if (typeof PoyntTerminal !== 'undefined' && PoyntTerminal.makeServerCall) {
      var headers = JSON.stringify({
        'Content-Type': 'x-Visa-II/x-auth',
        'Registration-Key': registrationKey
      });

      PoyntTerminal.makeServerCall(callbackId,
        $('#vt-form-businesstype').val() === 'MERCHANT' ? PROD_ELAVON_URL : TEST_ELAVON_URL, headers,
        'application/json; charset=utf-8', bodyString);

      $(window).on(callbackId, function (_, body) {
        $(window).off(callbackId);
        next(null, parseElavonToken(body.toString('utf-8')));
      });

    } else {
      $.ajax({
        url: $('#vt-form-businesstype').val() === 'MERCHANT' ? PROD_ELAVON_URL : TEST_ELAVON_URL,
        type: 'POST',
        headers: {
          'Content-Type': 'x-Visa-II/x-auth',
          'Registration-Key': registrationKey
        },
        encoding: null,
        contentType: 'x-Visa-II/x-auth',
        data: bodyString,
      }).done(function (body) {
        next(null, parseElavonToken(body.toString('utf-8')));
      }).fail(function (err) {
        next(err);
      });
    }
  };

  // decorator for entering currency values like a real terminal
  var values = {};
  var addCurrencyMask = function (elem, dollar) {
    values[elem] = $(elem).val() || 0;

    /**
     * Writes in currency into input.
     */
    var fillElem = function () {
      var amount = values[elem] / 100;
      if (currency === 'USD' && window.language.toLowerCase().match(/^en/) && !dollar) {
        $(elem).val(amount.toLocaleString(window.language, {
          style: 'decimal',
          minimumFractionDigits: 2
        }));
      } else {
        $(elem).val(amount.toLocaleString(window.language, {
          style: 'currency',
          currency: currency,
          currencyDisplay: 'symbol',
          minimumFractionDigits: 2
        }));
      }
    };

    if (values[elem]) {
      fillElem();
    }

    $(elem).on('keyup', function (e) {
      if (e.keyCode === 9) {
        return;
      } else if (e.shiftKey || (e.keyCode && e.keyCode !== 8 && (e.keyCode < 48 || e.keyCode > 57))) {
        return e.preventDefault();
      }

      if (e.keyCode === 8) {
        values[elem] = Math.floor(values[elem] / 10);
      } else if (e.keyCode >= 48 && e.keyCode <= 57) {
        values[elem] = values[elem] * 10 + (e.keyCode - 48);
      }

      if (values[elem] > 9999999) {
        values[elem] = 9999999;
      }

      fillElem();

      e.preventDefault();
    });

    $(elem).on('input', function (e) {
      setTimeout(function () {
        if ((values[elem] && $(elem).val().split('.')[1] && $(elem).val().split('.')[1].length === 1)) {
          values[elem] = Math.floor(values[elem] / 10);
        }

        fillElem();

        e.preventDefault();
      }, 100);
    });
  };

  addCurrencyMask('#vt-form-amount');
  addCurrencyMask('#vt-form-tip', true);
  addCurrencyMask('#vt-form-capture-tip', true);

  // disallow double submission of forms
  var charging = false;

  // elavon card presence UI logic
  if (window.isElavon) {
    $('#vt-form').removeClass('active');
    $('#card-presence-div').show();
  } else {
    // just show the form
    $('#vt-form').addClass('active');
  }

  // card not present button
  $('#vt-form-card-not-present').on('click', function (e) {
    $('#card-presence-div').hide();
    $('#vt-form').addClass('active');

    if (window.isLodging || (window.isElavon && $('#vt-form-force-refund').val())) {
      // https://jira.poynt.com/browse/MX-357
      // https://jira.poynt.com/browse/MX-358
      $('#vt-form-street').prop('disabled', true);
      $('#vt-form-street-box').addClass('disabled');
    }
  });

  // card present button
  $('#vt-form-card-present').on('click', function (e) {
    $('#card-presence-div').hide();
    $('#vt-form').addClass('active');
    cardPresent = true;

    if (window.isLodging) {
      // https://jira.poynt.com/browse/MX-700
      $('#vt-form-street').prop('disabled', true);
      $('#vt-form-street-box').addClass('disabled');
    } else if (window.isElavon && $('#vt-form-force-refund').val()) {
      // https://jira.poynt.com/browse/MX-357
      $('#vt-form-street').prop('disabled', false);
      $('#vt-form-street-box').removeClass('disabled');
    }
  });

  // auto tab from card when valid
  $('#vt-form-card').on('keydown', function () {
    setTimeout(function () {
      var number = $('#vt-form-card').val();
      if (!number) {
        return;
      }
      if (!window.iOS && number.replace(/ /g, '').length >= 15 && $.payment.validateCardNumber(number)) {
        $('#vt-form-exp').focus();
      }

      if (!saleMode) {} else if (cardPresent) {
        var cardType = $.payment.cardType(number);
        if (cardType === 'visa') {
          $('#vt-form-cvc').prop('disabled', true);
          $('#vt-form-cvc-box').addClass('disabled');
        } else {
          $('#vt-form-cvc').prop('disabled', false);
          $('#vt-form-cvc-box').removeClass('disabled');
        }
      } else {
        $('#vt-form-cvc').prop('disabled', false);
        $('#vt-form-cvc-box').removeClass('disabled');
      }
    });
  });

  // auto tab from expiration date when valid
  $('#vt-form-exp').on('keydown', function () {
    setTimeout(function () {
      var exp = $.payment.cardExpiryVal($('#vt-form-exp').val());
      if (!exp) {
        return;
      }
      if (!window.iOS && $.payment.validateCardExpiry(exp.month, exp.year)) {
        if ($('#vt-form-cvc').is(':disabled')) {
          if (!$('#vt-form-zip').length || $('#vt-form-zip').is(':disabled')) {
            $('#vt-form-street').focus();
          }
          $('#vt-form-zip').focus();
        } else if ($('#vt-form-cvc').length) {
          $('#vt-form-cvc').focus();
        } else {
          $('#vt-form-street').focus();
        }
      }
    });
  });

  // auto tab from cvc when valid
  $('#vt-form-cvc').on('keydown', function () {
    setTimeout(function () {
      var cvc = $('#vt-form-cvc').val();
      if (!cvc) {
        return;
      }
      if (!window.iOS && $.payment.validateCardCVC(cvc)) {
        var cardType = $.payment.cardType($('#vt-form-card').val());
        if ((!cardType || cardType === 'amex') && cvc.length === 4) {
          $('#vt-form-zip').focus();
        } else if (cardType && cardType !== 'amex' && cvc.length === 3) {
          $('#vt-form-zip').focus();
        }
      }
    });
  });

  // auto tab from zip when valid
  $('#vt-form-zip').on('keydown', function () {
    setTimeout(function () {
      var zip = $('#vt-form-zip').val();
      if (!window.iOS && !isNaN(zip) && zip.length === 5) {
        $('#vt-form-street').focus();
      }
    });
  });

  // shake form when invalid
  var formFail = function (text) {
    $('#vt-form').addClass('invalid');
    setTimeout(function () {
      $('#vt-form').removeClass('invalid');
    }, 350);
    if (text) {
      $('#vt-form-error').text(text);
      $('#vt-form-error').addClass('active');
    }
    return false;
  };

  // print error message from api call
  var formError = function (err) {
    var errMsg;
    try {
      errMsg = JSON.parse(err.responseText);
      errMsg = errMsg && errMsg.message || errMsg;
      var errSubMsg;
      try {
        errSubMsg = JSON.parse(errMsg);
        if (errSubMsg.error && errSubMsg.error.message) {
          errMsg = errSubMsg.error.message;
        }
      } catch (e) {}
    } catch (e) {
      errMsg = err;
    }
    return errMsg;
  };

  function getCardIcon(fundingSource) {
    if (fundingSource.card) {
      switch (fundingSource.card.type) {
      case 'AMERICAN_EXPRESS':
        return 'fa-cc-amex';
      case 'DISCOVER':
        return 'fa-cc-discover';
      case 'MASTER_CARD':
        return 'fa-cc-mastercard';
      case 'VISA':
        return 'fa-cc-visa';
      case 'I':
        return 'fa-cc-diners-club';
      case 'J':
        return 'fa-cc-jcb';
      default:
        return 'fa-credit-card-alt';
      }
    }
    return 'fa-money';
  }

  // authonly checkbox
  var authLabel = $('#vt-form-auth-label');
  var authToggleDiv = $('#vt-form-auth-toggle');

  // $('#vt-form-authonly').change(function() {
  //   if (this.checked) {
  //     authLabel.addClass('auth-checked');
  //   } else {
  //     authLabel.removeClass('auth-checked');
  //   }
  // });

  var enableSaleMode = function () {
    saleMode = true;
    authToggleDiv.css('opacity', '1');
    submitButton.removeClass('refund-btn');
    submitButton.html(window.strings.TRANSACTION.charge);
    amountForm.css('display', 'block');
    refundForm.css('display', 'none');

    if (window.isElavon) {
      $('#vt-form-cvc').prop('disabled', false);
      $('#vt-form-cvc-box').removeClass('disabled');
      $('#vt-form-zip').prop('disabled', false);
      $('#vt-form-zip-box').removeClass('disabled');

      if (!window.isLodging) {
        $('#vt-form-street').prop('disabled', false);
        $('#vt-form-street-box').removeClass('disabled');
      }
    }
  };

  var enableRefundMode = function () {
    saleMode = false;
    authToggleDiv.css('opacity', '0');
    submitButton.addClass('refund-btn');
    submitButton.html(window.strings.TRANSACTION.refund);
    amountForm.css('display', 'none');
    refundForm.css('display', 'block');

    if (window.isElavon) {
      // https://jira.poynt.com/browse/MX-358
      $('#vt-form-cvc').val('');
      $('#vt-form-cvc').prop('disabled', true);
      $('#vt-form-cvc-box').addClass('disabled');
      $('#vt-form-zip').val('');
      $('#vt-form-zip').prop('disabled', true);
      $('#vt-form-zip-box').addClass('disabled');

      if (!cardPresent) {
        $('#vt-form-street').prop('disabled', true);
        $('#vt-form-street-box').addClass('disabled');
      }
    }

    if (!enabledRefund) {
      enabledRefund = true;
      addCurrencyMask('#vt-refund-form-amount', true);
      refundInput.val('');
    }
  };

  var typeSelect = $('#vt-form-type-select');
  typeSelect.change(function (e) {
    if (ACTION !== typeSelect.val()) {
      ACTION = typeSelect.val();
      if (ACTION === 'sale') {
        enableSaleMode();
        typeSelect.removeClass('refund');
        typeSelect.removeClass('auth');
      } else if (ACTION === 'auth') {
        enableSaleMode();
        typeSelect.removeClass('refund');
        typeSelect.addClass('auth');
      } else if (ACTION === 'refund') {
        enableRefundMode();
        typeSelect.removeClass('auth');
        typeSelect.addClass('refund');
      }
    }
  });

  var cancel = function (e, flag) {
    if (e && e.preventDefault) {
      e.preventDefault();
    }

    if (typeof PoyntTerminal !== 'undefined' && PoyntTerminal.transactionCanceled) {
      PoyntTerminal.transactionCanceled();
    }

    if (window.paymentFragment) {
      return false;
    }

    if (dccData) {
      $('#vt-dcc-form').removeClass('active');
      dccData = null;
    }

    $('#vt-form-authonly').prop('checked', false);
    authLabel.removeClass('auth-checked');
    $('#vt-form-amount').val('');
    $('#vt-refund-form-amount').val('');
    $('#vt-form-tip').val('');
    $('#vt-form-card').val('');
    $('#vt-form-exp').val('');
    $('#vt-form-cvc').val('');
    $('#vt-form-zip').val('');
    $('#vt-form-street').val('');
    $('#vt-form-note').val('');

    values['#vt-form-amount'] = 0;
    values['#vt-form-tip'] = 0;

    $('#vt-form-charging').fadeOut();
    $('#vt-form-error').text('');
    $('#vt-form-error').removeClass('active');

    $('#vt-form-capturing').fadeOut();
    $('#vt-form-voiding').fadeOut();
    $('#vt-intermediate-error').text('');
    $('#vt-intermediate-error').removeClass('active');

    // if we're canceling because of a decline
    if (flag === DISPLAY_ERROR_FLAG) {
      $('#vt-form').addClass('active');
    } else {
      // if we're canceling because of a button click
      // elavon card presence UI logic
      if (window.isElavon) {
        // reload the page so we can refresh the token
        window.location.reload();
      } else {
        // just show the form
        $('#vt-form').addClass('active');
      }
    }

    $('#vt-complete-dialog').removeClass('active');
    $('#vt-intermediate-dialog').removeClass('active');

    $('#vt-complete-receipt-text').text(window.strings.COMPLETE.receipt + '?');

    if (saleMode) {
      $('#vt-form-amount').focus();
    } else {
      $('#vt-refund-form-amount').focus();
    }
    return false;
  };

  if (!window.iOS) {
    // clear form
    $('#vt-form-cancel').click(cancel);
    $('#vt-form-finished').click(cancel);
    $('#vt-form-new').click(cancel);
    $('#vt-dcc-cancel').click(cancel);
    $('#vt-call-auth-cancel').click(cancel);
  }

  // print button
  $('#vt-complete-print-btn').on('click', function (e) {
    var lastTxnId = $('#vt-form-transactionid').val();
    if (typeof PoyntTerminal !== 'undefined' && PoyntTerminal.printReceipt && lastTxnId) {
      PoyntTerminal.printReceipt(lastTxnId);
      $('#vt-complete-receipt-text').text(window.strings.COMPLETE.receiptPrinted);
    }
  });

  // email button
  $('#vt-complete-email-btn').on('click', function (e) {
    $('#vt-complete-receipt-options').hide();
    $('#vt-complete-buttons').hide();
    $('#vt-email-receipt').show();
    $('#vt-email-receipt-buttons').show();
    $('#vt-email-receipt-input').focus();
  });

  var hideEmailReceipt = function () {
    $('#vt-complete-receipt-options').show();
    $('#vt-complete-buttons').show();
    $('#vt-email-receipt').hide();
    $('#vt-email-receipt-buttons').hide();
  };

  $('#vt-email-receipt-cancel').on('click', function () {
    hideEmailReceipt();
  });

  $('#vt-email-receipt-send').on('click', function () {
    $('#vt-email-receipt-sending').fadeIn(200);

    $.post(window.webHost + '/api/t/receipts', {
      txn_uuid: $('#vt-form-transactionid').val(),
      email_address: $('#vt-email-receipt-input').val()

    }, function () {
      $('#vt-email-receipt-sending').fadeOut(200);
      $('#vt-complete-receipt-text').text(window.strings.COMPLETE.receiptSentViaEmail);
      hideEmailReceipt();

    }).fail(function () {
      $('#vt-email-receipt-sending').fadeOut(200);
      $('#vt-complete-receipt-text').text(window.strings.COMPLETE.emailFailedTryAgain);
      hideEmailReceipt();
    });
  });

  $('#vt-intermediate-continue').click(function (e) {
    $('#vt-intermediate-dialog').removeClass('active');
    $('#vt-complete-dialog').addClass('active');
  });

  // capture an intermediate charge
  $('#vt-intermediate-capture').click(function (e) {
    e.preventDefault();
    if (charging) {
      return false;
    }

    charging = true;
    $('#vt-capturing').fadeIn(200);
    $('#vt-form-error').removeClass('active');

    $.post('/virtualterminal/' + encodeURIComponent($('#vt-form-transactionid').val()) + '/capture', {
      authToken: $('#vt-form-authtoken').val(),
      _csrf: $('#vt-form-csrf').val(),
      businessId: $('#vt-form-businessid').val(),
      storeId: $('#vt-form-storeid').val(),
      deviceId: $('#vt-form-deviceid').val()

    }, function (data) {
      charging = false;
      $('#vt-capturing').fadeOut(200);
      $('#vt-intermediate-error').text('');
      $('#vt-intermediate-error').removeClass('active');

      if (data && data.status === 'CAPTURED') {
        /* jshint ignore:start */
        if (typeof PoyntTerminal !== 'undefined' && PoyntTerminal.transactionProcessed) {
          PoyntTerminal.transactionProcessed('CAPTURED', data.id);
          if (data.auth) {
            PoyntTerminal.transactionProcessed('CAPTURED', data.auth);
          }
        }
        /* jshint ignore:end */
        $('#vt-complete-header-text').text(window.strings.COMPLETE.completed);
        $('#vt-intermediate-dialog').removeClass('active');
        $('#vt-complete-dialog').addClass('active');

      } else {
        charging = false;
        $('#vt-capturing').fadeOut(200);
        $('#vt-intermediate-error').text(window.strings.COMPLETE[(data && data.status || '').toLowerCase()]);
        $('#vt-intermediate-error').addClass('active');
      }

    }, 'json').fail(function (err) {
      charging = false;
      var errMsg = formError(err);
      $('#vt-capturing').fadeOut(200);
      $('#vt-intermediate-error').text(errMsg);
      $('#vt-intermediate-error').addClass('active');
    });

    return false;
  });

  // authorize or refund a charge
  $('#vt-form').submit(function () {
    if (charging) {
      return false;
    }

    var authOnly = ACTION === 'auth' || $('#vt-form-force-authonly').val() === 'true' || $(
      '#vt-form-force-verify').val() === 'true'; // || $('#vt-form-authonly').is(':checked');
    var verify = $('#vt-form-force-verify').val() === 'true';
    var number = $('#vt-form-card').val();
    var exp = $.payment.cardExpiryVal($('#vt-form-exp').val());
    var cvc = $('#vt-form-cvc').val();
    var zip = ($('#vt-form-zip').val() || '').toUpperCase();
    var street = $('#vt-form-street').val();
    var notes = $('#vt-form-note').val();
    var amount = saleMode ? values['#vt-form-amount'] : values['#vt-refund-form-amount'];
    var tip = values['#vt-form-tip'];

    if (!$.payment.validateCardNumber(number)) {
      $('#vt-form-card').select();
      return formFail(window.strings.ERRORS.invalidCardNumber);
    }

    if (!$.payment.validateCardExpiry(exp && exp.month, exp && exp.year)) {
      $('#vt-form-exp').select();
      return formFail(window.strings.ERRORS.invalidExpirationDate);
    }

    if (!window.isLodging && !cardPresent && saleMode && (!cvc || (cvc && !$.payment.validateCardCVC(cvc)))) {
      $('#vt-form-cvc').select();
      return formFail(window.strings.ERRORS.invalidCvv);
    }

    // validate the amount if it's not paymentFragment
    if (!window.paymentFragment) {
      if (saleMode) {
        if (!amount) {
          amount = 0;
        }

        if (isNaN(amount)) {
          $('#vt-form-amount').select();
          return formFail(window.strings.ERRORS.invalidAmount);
        }

        if (tip && isNaN(tip)) {
          $('#vt-form-tip').select();
          return formFail(window.strings.ERRORS.invalidTipAmount);
        }
      } else {
        if (!amount || isNaN(amount)) {
          $('#vt-refund-form-amount').select();
          return formFail(window.strings.ERRORS.invalidRefundAmount);
        }
      }
    }

    charging = true;
    $('#vt-authorizing').fadeIn(200);
    $('#vt-form-error').removeClass('active');

    var transactionObject = {
      sale: saleMode,
      authOnly: authOnly,
      verify: verify,
      number: number.replace(/ /g, ''),
      exp: exp,
      zip: zip,
      street: street,
      notes: notes,
      amount: amount,
      tip: tip,
      _csrf: $('#vt-form-csrf').val(),
      authToken: $('#vt-form-authtoken').val(),
      currency: $('#vt-form-currency').val(),
      businessId: $('#vt-form-businessid').val(),
      storeId: $('#vt-form-storeid').val(),
      deviceId: $('#vt-form-deviceid').val(),
      purchaseAction: $('#vt-form-purchaseaction').val(),
      paymentFragment: window.paymentFragment
    };

    if (cvc) {
      transactionObject.cvc = cvc;
    }
    if (cardPresent) {
      transactionObject.cardPresent = true;
    }

    if ($('#vt-form-order-id').val()) {
      transactionObject.orderId = $('#vt-form-order-id').val();
    }

    if ($('#vt-form-session-id').val()) {
      transactionObject.sessionId = $('#vt-form-session-id').val();
    }

    var successHandler = function successHandler(data) {
      charging = false;

      var transaction = data.transaction;
      $('#vt-form-transactionid').val(transaction && transaction.id || '');

      $('#vt-form-error').text('');
      $('#vt-form-error').removeClass('active');
      $('#vt-dcc-form-error').text('');
      $('#vt-dcc-form-error').removeClass('active');
      $('#vt-call-auth-error').text('');
      $('#vt-call-auth-error').removeClass('active');

      // https://jira.poynt.com/browse/MX-702
      // if elavon non lodging, show avs info
      if (!data.skipAvs && window.isElavon && !window.isLodging && (transaction.status === 'AUTHORIZED' ||
          transaction.status === 'CAPTURED')) {
        var showAvsDialog = false;

        var ELAVON_AVS_STRINGS = {
          A: 'Address (street) matches, Zip does not',
          B: 'Street address match, postal code in wrong format',
          C: 'Street address and postal code in wrong formats',
          D: 'Street address and postal code match',
          E: 'AVS error',
          F: 'Address does compare and five-digit Zip code does compare',
          G: 'Card issued by a non-US issuer that does not participate in the AVS system',
          I: 'Address information not verified by international issuer',
          M: 'Street address and postal code match',
          N: 'No match on address (street) or Zip',
          P: 'Postal codes match, street address not verified due to incompatible formats',
          R: 'Retry, system unavailable or timed out',
          S: 'Service not supported by issuer',
          U: 'Address information is unavailable',
          W: '9 digit Zip matches, address (street) does not',
          X: 'Exact AVS match',
          Y: 'Address (street) and 5 digit Zip match',
          Z: '5 digit Zip matches, address (street) does not',
        };

        // avs code
        var avsCode = transaction.processorResponse && transaction.processorResponse.avsResult && transaction
          .processorResponse.avsResult.actualResult;
        var streetNotMatched = street && transaction.processorResponse && transaction.processorResponse.avsResult &&
          transaction.processorResponse.avsResult.addressResult !== 'MATCH';
        var zipNotMatched = zip && transaction.processorResponse && transaction.processorResponse.avsResult &&
          transaction.processorResponse.avsResult.postalCodeResult !== 'MATCH';
        if ((streetNotMatched || zipNotMatched) && ['D', 'F', 'M', 'X', 'Y'].indexOf(avsCode) !== -1) {
          $('#vt-avs-header-avs').text('Not Match');
          $('#vt-avs-details-avs').text(ELAVON_AVS_STRINGS[avsCode] || '');
          showAvsDialog = true;
        } else if (street || zip) {
          $('#vt-avs-header-avs').text('Match');
          $('#vt-avs-details-avs').text('');
        } else {
          $('#vt-avs-header-avs').text('Not Entered');
          $('#vt-avs-details-avs').text('');
        }

        // v code
        var cvResult = transaction.processorResponse && transaction.processorResponse.cvResult;
        if (cvc && cvResult !== 'MATCH') {
          $('#vt-avs-header-v').text('Not Match');
          $('#vt-avs-details-v').text(cvResult);
          showAvsDialog = true;
        } else if (cvc) {
          $('#vt-avs-header-v').text('Match');
          $('#vt-avs-details-v').text('');
        } else {
          $('#vt-avs-header-v').text('Not Entered');
          $('#vt-avs-details-v').text('');
        }

        // show the thang
        if (showAvsDialog) {
          $('#vt-authorizing').fadeOut(200);
          $('#vt-form').removeClass('active');
          if (window.iOS || typeof PoyntTerminal !== 'undefined') {
            $('#vt-avs-dialog').css({
              'width': '100%'
            });
          }
          $('#vt-call-auth').removeClass('active');
          $('#vt-avs-dialog').addClass('active');

          // continue with avs txn
          // bind handler here since we need to call this fxn again
          $('#vt-avs-continue').unbind('click').click(function (e) {
            e.preventDefault();
            if (charging) {
              return false;
            }

            $('#vt-avs-dialog').removeClass('active');

            data.skipAvs = true;
            successHandler(data);
          });

          return;
        }
      }

      if (transaction && (transaction.status === 'AUTHORIZED' || transaction.status === 'CAPTURED' ||
          transaction.status === 'REFUNDED')) {

        if (typeof PoyntTerminal !== 'undefined' && PoyntTerminal.transactionProcessed) {
          if (transaction.status === 'CAPTURED') {
            PoyntTerminal.transactionProcessed('CAPTURED', transaction.id);
            if (transaction.auth) {
              PoyntTerminal.transactionProcessed('CAPTURED', transaction.auth);
            }
          } else if (transaction.status === 'AUTHORIZED') {
            PoyntTerminal.transactionProcessed('AUTHORIZED', transaction.id);
          } else if (transaction.status === 'REFUNDED') {
            PoyntTerminal.transactionProcessed('REFUNDED', transaction.id);
          }
        }

        if (window.paymentFragment ||
          $('#vt-form-callback').val() === 'true') {
          $('#vt-form').fadeOut(200);
          return;
        } else {
          $('#vt-authorizing').fadeOut(200);
          $('#vt-dcc-authorizing').fadeOut(200);
          $('#vt-call-auth-authorizing').fadeOut(200);
          $('#vt-form').removeClass('active');
          $('#vt-dcc-form').removeClass('active');
          $('#vt-call-auth').removeClass('active');
        }

        if ($('#vt-form-callback').val() === 'true') {
          return;
        }

        var total = (amount + tip) * 1.0 / 100;
        var amountString = total.toLocaleString(window.language, {
          style: 'currency',
          currency: currency,
          currencyDisplay: 'symbol',
          minimumFractionDigits: 2
        });
        var cardNumber = '&bull;&bull;&bull;&bull; ' + transaction.fundingSource.card.numberLast4;

        if (transaction.status === 'REFUNDED') {
          $('#vt-complete-header').removeClass('authorized');
          $('#vt-complete-header').addClass('refunded');
        } else if (transaction.status === 'AUTHORIZED') {
          $('#vt-complete-header').removeClass('refunded');
          $('#vt-complete-header').addClass('authorized');
        }

        $('#vt-complete-header-text').text(transaction.status === 'CAPTURED' ? window.strings.COMPLETE.completed :
          window.strings.COMPLETE.authorized);
        if (transaction.status === 'REFUNDED') {
          $('#vt-complete-header-text').text(window.strings.COMPLETE.refunded);
        }
        $('#vt-complete-time').text(new Date(transaction.createdAt).toLocaleString(window.language));
        $('#vt-complete-card-type').addClass(getCardIcon(transaction.fundingSource));
        $('#vt-complete-card-num').html(cardNumber);
        if (transaction.notes) {
          $('#vt-complete-note').text(transaction.notes);
        }

        if (saleMode && data.partialApproval) {
          $('#vt-intermediate-time').text(new Date(transaction.createdAt).toLocaleString(window.language));
          $('#vt-intermediate-card-type').addClass(getCardIcon(transaction.fundingSource));
          $('#vt-intermediate-card-num').html(cardNumber);

          var partialApproval = data.partialApproval * 1.0 / 100;
          var partialString = partialApproval.toLocaleString(window.language, {
            style: 'currency',
            currency: currency,
            currencyDisplay: 'symbol',
            minimumFractionDigits: 2
          });
          $('#vt-intermediate-amt').text(partialString);
          $('#vt-intermediate-message-total').text(amountString);
          $('#vt-intermediate-message-amt').text(partialString);

          $('#vt-intermediate-capture').text(window.strings.INTERMEDIATE.continue+' ' + partialString);
          $('#vt-intermediate-continue').text(window.strings.INTERMEDIATE.continue+' ' + partialString);
          if (data.captureAfter) {
            $('#vt-intermediate-capture').show();
            $('#vt-intermediate-continue').hide();
          } else {
            $('#vt-intermediate-capture').hide();
            $('#vt-intermediate-continue').show();
          }

          $('#vt-complete-amt').text(partialString);
          $('#vt-intermediate-dialog').addClass('active');

        } else {
          $('#vt-complete-amt').text((saleMode ? '' : '-') + amountString);
          $('#vt-complete-dialog').addClass('active');
        }

      } else {
        charging = false;
        $('#vt-authorizing').fadeOut(200);
        $('#vt-dcc-authorizing').fadeOut(200);
        $('#vt-call-auth-authorizing').fadeOut(200);
        $('#vt-form-error').text(window.strings.COMPLETE[(transaction && transaction.status || '').toLowerCase()]);
        $('#vt-form-error').addClass('active');
      }
    }; // end of successHandler function

    // handler for Call Auth Center Error
    var callAuthHandler = function callAuthHandler(data) {
      $('#vt-form').removeClass('active');
      $('#vt-form-error').removeClass('active');
      $('#vt-dcc-form').removeClass('active');
      $('#vt-dcc-form-error').removeClass('active');

      if (window.iOS || typeof PoyntTerminal !== 'undefined') {
        $('#vt-call-auth').css({
          'width': '100%'
        });
        $('#vt-call-auth-btn-row').css({
          'text-align': 'center'
        });
        $('#vt-call-auth-cancel').hide();
        $('.vt-powered').hide();
      }
      $('#vt-call-auth').addClass('active');
    };

    var failureHandler = function failureHandler(err) {
      charging = false;
      $('#vt-authorizing').fadeOut(200);
      $('#vt-dcc-authorizing').fadeOut(200);
      $('#vt-call-auth-authorizing').fadeOut(200);
      $('#vt-dcc-form-agreement').hide();

      // if the error is Call Auth, then redirect menu to Call Auth Menu
      if (err.responseJSON.message === 'DECLINED: CALL AUTH CENTER') {
        callAuthHandler(transactionObject);
        return false;
      }

      // for debugging
      if (transactionObject.number === '4111111111111111' && transactionObject.amount === 1234) {
        $('#vt-form-error').text(err.status + ' typeof ' + typeof err.status);
        $('#vt-form-error').addClass('active');
        setTimeout(function () {
          $('#vt-form-error').text(JSON.stringify(err));
        }, 2000);
      } else {
        if (err.status === 0) {
          cancel(null, DISPLAY_ERROR_FLAG);
          $('#vt-dcc-form-error').text(window.strings.ERRORS.connectionError);
          $('#vt-dcc-form-error').addClass('active');
          $('#vt-form-error').text(window.strings.ERRORS.connectionError);
          $('#vt-form-error').addClass('active');
          $('#vt-call-auth-error').text(window.strings.ERRORS.connectionError);
          $('#vt-call-auth-error').addClass('active');
        } else {
          var errMsg = formError(err);
          $('#vt-dcc-form-error').text(errMsg);
          $('#vt-dcc-form-error').addClass('active');
          $('#vt-form-error').text(errMsg);
          $('#vt-form-error').addClass('active');
          $('#vt-call-auth-error').text(errMsg);
          $('#vt-call-auth-error').addClass('active');
        }
      }
    };

    // handler for dcc
    var dccHandler = function dccHandler(data) {
      charging = false;
      dccData = data;

      // check if card has a valid exchange rate, if it's 0, then we can just proceed as normal auth
      if (data.cardAmount !== 0 && data.cardCurrency) {
        // conversion for local amount, card amount, markup % to the appropriate format
        var localAmount = data.txnAmount * 1.0 / 100;
        var localAmountString = localAmount.toLocaleString(window.language, {
          style: 'currency',
          currency: data.txnCurrency,
          currencyDisplay: 'symbol',
          minimumFractionDigits: 2
        });

        var cardAmount = data.cardAmount * 1.0 / 100;
        var cardAmountString = cardAmount.toLocaleString(window.language, {
          style: 'currency',
          currency: data.cardCurrency,
          currencyDisplay: 'symbol',
          minimumFractionDigits: 2
        });

        var markupPercentageString = (data.markupPercentage * 1.0 / 100) + '%';
        var rate = (data.rate * 1.0 / Math.pow(10, data.ratePrecision)).toFixed(2);

        // flag according to its currency
        var flagLeft = '<div class="flag flag-' + data.txnCurrency.toLowerCase() + '">' + '</div>';
        var flagRight = '<div class="flag flag-' + data.cardCurrency.toLowerCase() + '">' + '</div>';

        // update text for each button
        $('#vt-dcc-form-btn-left').html('<div id="vt-dcc-form-btn-text-top">' + flagLeft + '<h2>' + data.txnCurrency +
          '</h2></div><h1>' + localAmountString + '</h1>');
        $('#vt-dcc-form-btn-right').html('<div id="vt-dcc-form-btn-text-top">' + flagRight + '<h2> ' + data.cardCurrency +
          '</h2></div><h1>' + cardAmountString + '</h1>');
        $('#vt-dcc-form-conversion').html('<div><h2>' + markupPercentageString + ' Conversion Fee' + '</h2>' +
          '<h2>1 ' + data.txnCurrency + ' = ' + rate + ' ' + data.cardCurrency + '</h2></div>');

        // remove elements that aren't needed for iOS or terminal screen
        if (window.iOS || typeof PoyntTerminal !== 'undefined') {
          $('.vt-powered').hide();
        }

        $('#vt-authorizing').fadeOut(200);
        $('#vt-form').removeClass('active');
        $('#vt-form-error').removeClass('active');
        $('#vt-dcc-form-error').removeClass('active');
        $('#vt-dcc-form').addClass('active');
      } else {
        $.post('/virtualterminal', transactionObject, successHandler, 'json').fail(failureHandler);
      }
    };

    // local currency button selected
    $('#vt-dcc-form-btn-left').on('click', function () {
      // change amount, tip, and currency according to local currency
      // amount is total transaction excluding the tip
      tip = dccData.tipAmount ? dccData.tipAmount : 0;
      amount = dccData.txnAmount - tip;
      currency = dccData.txnCurrency;

      // remove exchangeRate if user chooses not to use DCC
      if (transactionObject.exchangeRate) {
        transactionObject.exchangeRate = null;
      }

      // mark chosen button as white background while the other blue color background
      $('#vt-dcc-form-btn-left').css({
        'background-color': 'white',
        'color': 'black'
      });
      $('#vt-dcc-form-btn-right').css({
        'background-color': '#416A99',
        'color': '#8BCEEA'
      });

      // remove elements that aren't needed for iOS or terminal screen
      if (window.iOS || typeof PoyntTerminal !== 'undefined') {
        $('#vt-dcc-form-agreement').css({
          'width': '368px',
          'text-align': 'right'
        });
        $('#vt-dcc-agree').css({
          'margin': 'auto 20px 20px 50px',
          'font-weight': '600'
        });
        $('.vt-dcc-form-agreement-btn').css({
          'margin': 'auto 10px 20px 75px'
        });
        $('#vt-dcc-form-header').hide();
        $('#vt-dcc-cancel').hide();
        $('.vt-powered').hide();
      }

      // get unshortened currency of cardCurrency
      var txnCurrency = window.localizationCurrency[dccData.txnCurrency].name;

      // show agreement text and hide conversion info
      $('#vt-dcc-form-conversion').hide();
      $('#vt-form-error').removeClass('active');
      $('#vt-dcc-form-error').removeClass('active');
      $('#vt-dcc-form-agreement-text').html('<h2>' + window.strings.DCC.dccAgreementText + txnCurrency +
        ' (' + dccData.txnCurrency + ').' + '</h2>');
      $('#vt-dcc-form-agreement').show();
    });

    // card currency button selected
    $('#vt-dcc-form-btn-right').on('click', function () {
      // change amount, tip, and currency according to card currency
      // amount is total transaction excluding the tip
      tip = dccData.cardTipAmount ? dccData.cardTipAmount : 0;
      amount = dccData.cardAmount - tip;
      currency = dccData.cardCurrency;

      // attach exchangeRate data if user chooses to do DCC (foreign currency)
      transactionObject.exchangeRate = dccData;

      // mark chosen button as white background while the other blue color background
      $('#vt-dcc-form-btn-right').css({
        'background-color': 'white',
        'color': 'black'
      });
      $('#vt-dcc-form-btn-left').css({
        'background-color': '#416A99',
        'color': '#8BCEEA'
      });

      // remove elements that aren't needed for iOS or terminal screen
      if (window.iOS || typeof PoyntTerminal !== 'undefined') {
        $('#vt-dcc-form-agreement').css({
          'width': '368px',
          'text-align': 'right'
        });
        $('#vt-dcc-agree').css({
          'margin': 'auto 20px 20px 50px',
          'font-weight': '600'
        });
        $('.vt-dcc-form-agreement-btn').css({
          'margin': 'auto 10px 20px 75px'
        });
        $('#vt-dcc-form-header').hide();
        $('#vt-dcc-cancel').hide();
        $('.vt-powered').hide();
      }

      // get unshortened currency of cardCurrency
      var cardCurrency = window.localizationCurrency[dccData.cardCurrency].name;

      // show agreement text and hide conversion info
      $('#vt-dcc-form-conversion').hide();
      $('#vt-form-error').removeClass('active');
      $('#vt-dcc-form-error').removeClass('active');
      $('#vt-dcc-form-agreement-text').html('<h2>' + window.strings.DCC.dccAgreementText + cardCurrency +
        ' (' + dccData.cardCurrency + ').' + '</h2>');
      $('#vt-dcc-form-agreement').show();
    });

    // dcc agreement button pressed
    $('#vt-dcc-agree').on('click', function () {
      charging = true;
      $('#vt-dcc-authorizing').fadeIn(200);
      $('#vt-form-error').removeClass('active');

      // upon agreement, make a transaction to the server
      $.post('/virtualterminal', transactionObject, successHandler, 'json').fail(failureHandler);
      return false;
    });

    // call auth center submit button pressed
    $('#vt-call-auth-submit').on('click', function () {
      var approvalCode = $('#vt-call-auth-input').val();

      // if approvalCode is not alphanumeric or the length is not 6, then return an error
      if (!approvalCode.match(/^[a-z0-9]+$/i) || approvalCode.length !== 6) {
        $('#vt-call-auth-error').text(window.strings.CALLAUTH.approvalCodeError);
        $('#vt-call-auth-error').addClass('active');
        return;
      }

      charging = true;
      $('#vt-call-auth-authorizing').fadeIn(200);

      transactionObject.approvalCode = approvalCode;

      // proceed txn as usual
      $.post('/virtualterminal', transactionObject, successHandler, 'json').fail(failureHandler);
      return false;
    });

    if (window.elavonData) {
      // for elavon
      // make request to get registration key
      $.get('/virtualterminal/elavon/registration-key?requestToken=' + window.elavonData.requestToken, function (
        data) {
        var cardNumber = transactionObject.number;
        var month = '' + transactionObject.exp.month;
        var year = '' + transactionObject.exp.year;
        year = year.slice(2, 4);

        var cardData = cardNumber + '=' + month + year;
        var string = window.elavonData.prefix + cardData + window.elavonData.postfix;
        var lrc = calculateLRC(string);
        string = string + lrc;

        // get the elavon token from elavon's api
        getElavonToken(data.registrationKey, string, function (err, token) {
          // Replace the card number with the token and make the usual call
          if (token) {
            transactionObject.number = token;
          }

          // check if this merchant accepts DCC
          if (window.enableDCC) {
            $.post('/virtualterminal/getexchangerates', transactionObject, dccHandler, 'json').fail(
              function () {
                // if get exchange Rates fails, we proceed with normal flow
                $.post('/virtualterminal', transactionObject, successHandler, 'json').fail(
                  failureHandler);
              });
          } else {
            $.post('/virtualterminal', transactionObject, successHandler, 'json').fail(failureHandler);
          }
        });

      }).fail(function (err) {
        charging = false;
        $('#vt-authorizing').fadeOut(200);
        cancel(null, DISPLAY_ERROR_FLAG);
        $('#vt-form').removeClass('active');
        $('#vt-error').text(window.strings.ERRORS.sessionExpiredPleaseTryAgain);
        $('#vt-error').addClass('active');
        setTimeout(function () {
          window.location.reload();
        }, 2500);
      });
    } else {
      // non elavon
      // check if this merchant accepts DCC
      if (window.enableDCC) {
        $.post('/virtualterminal/getexchangerates', transactionObject, dccHandler, 'json').fail(function () {
          // if get exchange Rates fails, we proceed with normal flow
          $.post('/virtualterminal', transactionObject, successHandler, 'json').fail(failureHandler);
        });
      } else {
        $.post('/virtualterminal', transactionObject, successHandler, 'json').fail(failureHandler);
      }
    }

    return false;
  });

  // void an intermediate charge
  $('#vt-intermediate-void').click(function (e) {
    e.preventDefault();
    if (charging) {
      return false;
    }

    charging = true;
    $('.vt-voiding').fadeIn(200);
    $('#vt-intermediate-error').removeClass('active');

    $.post('/virtualterminal/' + encodeURIComponent($('#vt-form-transactionid').val()) + '/void', {
      authToken: $('#vt-form-authtoken').val(),
      _csrf: $('#vt-form-csrf').val(),
      businessId: $('#vt-form-businessid').val(),
      storeId: $('#vt-form-storeid').val(),
      deviceId: $('#vt-form-deviceid').val()

    }, function (data) {
      charging = false;
      $('.vt-voiding').fadeOut(200);
      $('#vt-intermediate-error').text('');
      $('#vt-intermediate-error').removeClass('active');

      if (data && (data.status === 'VOIDED' || data.status === 'DECLINED')) {
        /* jshint ignore:start */
        if (typeof PoyntTerminal !== 'undefined' && PoyntTerminal.transactionProcessed) {
          PoyntTerminal.transactionProcessed('VOIDED', data.id);
          if (data.auth) {
            PoyntTerminal.transactionProcessed('VOIDED', data.auth);
          }
        }
        /* jshint ignore:end */
        cancel();

      } else {
        charging = false;
        $('.vt-voiding').fadeOut(200);
        $('#vt-intermediate-error').text(window.strings.COMPLETE[(data && data.status || '').toLowerCase()]);
        $('#vt-intermediate-error').addClass('active');
      }

    }, 'json').fail(function (err) {
      charging = false;
      var errMsg = formError(err);
      $('.vt-voiding').fadeOut(200);
      $('#vt-intermediate-error').text(errMsg);
      $('#vt-intermediate-error').addClass('active');
    });

    return false;
  });

  // void an avs txn
  $('#vt-avs-void').click(function (e) {
    e.preventDefault();
    if (charging) {
      return false;
    }

    charging = true;
    $('.vt-voiding').fadeIn(200);

    $.post('/virtualterminal/' + encodeURIComponent($('#vt-form-transactionid').val()) + '/void', {
      authToken: $('#vt-form-authtoken').val(),
      _csrf: $('#vt-form-csrf').val(),
      businessId: $('#vt-form-businessid').val(),
      storeId: $('#vt-form-storeid').val(),
      deviceId: $('#vt-form-deviceid').val()

    }, function (data) {
      charging = false;
      $('.vt-voiding').fadeOut(200);
      $('#vt-avs-error').text('');
      $('#vt-avs-error').removeClass('active');

      if (data && (data.status === 'VOIDED' || data.status === 'DECLINED')) {
        /* jshint ignore:start */
        if (typeof PoyntTerminal !== 'undefined' && PoyntTerminal.transactionProcessed) {
          PoyntTerminal.transactionProcessed('VOIDED', data.id);
          if (data.auth) {
            PoyntTerminal.transactionProcessed('VOIDED', data.auth);
          }
        }
        /* jshint ignore:end */
        $('#vt-avs-dialog').removeClass('active');
        cancel();

      } else {
        charging = false;
        $('.vt-voiding').fadeOut(200);
        $('#vt-avs-error').text(window.strings.COMPLETE[(data && data.status || '').toLowerCase()]);
        $('#vt-avs-error').addClass('active');
      }

    }, 'json').fail(function (err) {
      charging = false;
      var errMsg = formError(err);
      $('.vt-voiding').fadeOut(200);
      $('#vt-avs-error').text(errMsg);
      $('#vt-avs-error').addClass('active');
    });

    return false;
  });

  $(window).on('cardUpdated', function (evt, card) {
    if (card && card.card) {
      $('#vt-form-card').val(card.card);
      $('#vt-form-card').trigger('input');
    }
    if (card && card.exp) {
      $('#vt-form-exp').val(card.exp);
      $('#vt-form-exp').trigger('input');
    }
    if (card && card.cvv) {
      $('#vt-form-cvc').val(card.cvv);
      $('#vt-form-cvc').trigger('input');
    }
  });

  // auto focus on first field
  if ($('#vt-form-amount').val()) {
    if (!$('#vt-form-tip').length || $('#vt-form-tip').val()) {
      $('#vt-form-card').focus();
    } else {
      $('#vt-form-tip').focus();
    }
  } else {
    $('#vt-form-amount').focus();
  }

  if ($('#vt-form-force-refund').val()) {
    enableRefundMode();
  }
});
