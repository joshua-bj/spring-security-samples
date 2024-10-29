<?php
$metadata['http://sp.127-0-0-1.nip.io:8080/saml2/metadata'] = array(
    'AssertionConsumerService' => 'http://localhost:8080/login/saml2/sso',
    'SingleLogoutService' => 'http://localhost:8080/logout/saml2/slo',
    'NameIDFormat' => 'urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress',
    'simplesaml.nameidattribute' => 'emailAddress',
    'assertion.encryption' => FALSE,
    'nameid.encryption' => FALSE,
    'validate.authnrequest' => FALSE,
    'redirect.sign' => TRUE,
);
?>
