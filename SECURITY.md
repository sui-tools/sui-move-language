# Security Policy

## Supported Versions

We release patches for security vulnerabilities. Which versions are eligible for receiving such patches depends on the CVSS v3.0 Rating:

| Version | Supported          |
| ------- | ------------------ |
| 1.x.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

We take the security of our software seriously. If you believe you have found a security vulnerability in the Sui Move Language plugin, please report it to us as described below.

### Please do NOT:
- Open a public issue on GitHub
- Disclose the vulnerability publicly before it has been addressed

### Please DO:
- Email us at: support@sui-move.com (replace with your actual security email)
- Include the following information:
  - Type of vulnerability
  - Full paths of source file(s) related to the vulnerability
  - Location of the affected source code (tag/branch/commit or direct URL)
  - Step-by-step instructions to reproduce the issue
  - Proof-of-concept or exploit code (if possible)
  - Impact of the issue

### What to Expect:
1. **Acknowledgment**: We will acknowledge receipt of your vulnerability report within 48 hours
2. **Initial Assessment**: Within 7 days, we will provide an initial assessment of the vulnerability
3. **Resolution Timeline**: We will work with you to understand and resolve the issue promptly
4. **Disclosure**: Once the issue is resolved, we will work with you on responsible disclosure

### Security Best Practices for Users:
1. Keep the plugin updated to the latest version
2. Only install the plugin from official sources (JetBrains Marketplace or GitHub releases)
3. Review plugin permissions before installation
4. Report any suspicious behavior immediately

## Security Features

The Sui Move Language plugin implements several security measures:

1. **Input Validation**: All user inputs are validated and sanitized
2. **Secure Communication**: Plugin updates are downloaded over HTTPS
3. **Limited Permissions**: The plugin only requests necessary permissions
4. **No Data Collection**: The plugin does not collect or transmit user data

## Acknowledgments

We would like to thank the following individuals for responsibly disclosing security issues:

<!-- Add names of security researchers who have helped -->

Thank you for helping keep the Sui Move Language plugin and its users safe!
